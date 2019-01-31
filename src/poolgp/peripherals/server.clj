(ns poolgp.peripherals.server
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async]
            [poolgp.simulation.structs :as structs]
            [poolgp.simulation.utils :as utils]
            [poolgp.simulation.manager :as simulation-manager]
            [poolgp.simulation.players.manager :as player-manager]
            [poolgp.log :as log])
  (:import [java.net ServerSocket SocketException Socket InetSocketAddress])
  (:gen-class))

;holds new strings from socket
; channel worker adds to pool or in channel depending on type
(def SERVER-CHANNEL (async/chan))

;holds individuals to be tested, pulled off individually
(def IN-CHANNEL (async/chan))

;holds individuals that have already been tested
(def OUT-CHANNEL (async/chan))

;count of total individuals in system
(def TOTAL-INDIVS (atom 0))

;Detected ip for return packets
(def REMOTE-HOST (atom nil))

;Holds individuals being used as tests for individuals in test mode
(def OPPONENT-POOL (atom (list)))

;This is the current evaluation cycle: if a new cycle is detected the opponent pool is purged
(def CURRENT-CYCLE (atom 0))

(defn- status-task
  "runnable (thread) log process"
  []
  (loop []
    (do
      (log/write-info (str "Active threads: " (Thread/activeCount)))
      (log/write-info (str "Opponent pool size: " (count @OPPONENT-POOL)))
      (log/write-info (str "Current cycle: " @CURRENT-CYCLE))
      (log/write-info (str "Total individuals in system: " @TOTAL-INDIVS))
      (Thread/sleep 10000))
      (recur)))

(defn start-status-process
  "start a background thread that periodically logs"
  []
  (.start (Thread. status-task)))

(defn- async-persistent-server
  "start listening server, push individuals to channel"
  [socket]
  (log/write-info "Starting persistent async server...")
  (future
    (async/go-loop []
      (let [client-socket (.accept socket)]
       (try
         (if (nil? @REMOTE-HOST)
              (let [return-addr (.getHostName (.getInetAddress client-socket))]
                (log/write-info (str "Returning individuals to: " return-addr))
                (reset! REMOTE-HOST return-addr)))
            ;push to incoming channel
         (async/>! SERVER-CHANNEL (.readLine (io/reader client-socket)))
         (.close client-socket)
         (catch SocketException e
           (.close client-socket)))
        (recur)))))

(defn- add-players
  "updateplayer info in state"
  [starting-state p1 p2]
  (assoc starting-state
    :p1 (assoc
          (player-manager/init-player (assoc p1 :genetic true) :p1)
          :eval-id (:eval-id p1))
    :p2 (assoc
          (player-manager/init-player (assoc p2 :genetic true) :p2)
          :eval-id (:eval-id p2))))

(defn- run-simulation
  "run the current simulation state
  and output to outgoing channel"
  [starting-state test-indiv opponent]
  (async/go
    (let [max-cycles (:max-iterations starting-state)
          eval-state (add-players starting-state test-indiv opponent)
          resultant-state
              (loop [current 0
                     state eval-state]
                     (if (> max-cycles current)
                       ;(simulation-manager/simulation-log state)
                       (recur (inc current)
                              (doall (simulation-manager/simulation-update state)))
                     state))]
              ;return individual from state
              (:p1 resultant-state))))

(defn- server-channel-worker
  "listens on server channel. Checks if opponent or individuals
  and is responsible for purging the pool on cycle changes"
  []
  (log/write-info "Starting server channel worker...")
  (async/go-loop []
    (try
      (let [new-gp-indiv (read-string (async/<! SERVER-CHANNEL))]
            (do
              (if (= (:type new-gp-indiv) :opponent)
                ;add opponent to pool
                (swap! OPPONENT-POOL conj new-gp-indiv)
                ;add individual (to be tested) to in-channel for testing
                (do
                  (async/>! IN-CHANNEL new-gp-indiv)
                  ;update total
                  (swap! TOTAL-INDIVS inc)))
               (if (not (= (:cycle new-gp-indiv) @CURRENT-CYCLE))
                  (do
                    (log/write-info "Detected new cycle, purging opponent pool")
                    (reset! OPPONENT-POOL (list))
                    (reset! CURRENT-CYCLE (:cycle new-gp-indiv))))))
      (catch Exception e
        (log/write-error "Failed to read individual from server channel")))
    (recur)))

(defn- create-outgoing-map
  "take resulting gamestates and turn into report to return to engine"
  [indiv results-list]
  ;TODO: incorporate results list
  indiv
  )

(defn- in-channel-worker
  "start channel worker with starting state"
  [simulation-state]
  (log/write-info "Starting incoming channel worker...")
  (async/go-loop []
    (try
      (let [indiv (async/<! IN-CHANNEL)]
        (log/write-info (str "Running simulations on individual "
                              (:eval-id indiv) " against " (count @OPPONENT-POOL)
                              " opponents"))
        (async/>! OUT-CHANNEL
          (create-outgoing-map indiv
            (doall (map
                  (fn [op]
                    ;TODO: individual currently goes against itself
                    (run-simulation simulation-state indiv op))
                  @OPPONENT-POOL)))))
      (catch Exception e
        (log/write-error "Failed to evaluate individual on opponent pool")))
    (recur)))

(defn- out-channel-worker
  "start a distribution worker"
  [port]
  (log/write-info "Starting outgoing channel worker...")
  (async/go-loop []
    (let [player (async/<! OUT-CHANNEL)
          ;client-socket (Socket. @REMOTE-HOST port)
          ;writer (io/writer client-socket)
          ]
    ;TODO: do analytics aggregation
      ;(.connect client-socket (InetSocketAddress. host port))
      (log/write-info (str "Finished simulation cycle on: " (:eval-id player)))
      (swap! TOTAL-INDIVS dec)
      (log/write-info (str "Current individual count (in progress): " @TOTAL-INDIVS))
      ;(.write writer (pr-str player))
      )
      (recur)
    )
  )

(defn- display-starting-config
  "log starting configuration state"
  [s]
  (doall (map log/write-info
      (list
        (str "Listening on port: " (:port s))
        (str "Max iterations: " (:max-iterations s))
        (str "Total analysis-states: " (count (:analysis-states s)))))))

(defn start-server
  "start a persistent socket server"
  [task-def]
  (let [simulation-state (simulation-manager/simulation-init task-def false)
        socket (ServerSocket. (:port simulation-state))]
      (do
        (.setSoTimeout socket 0)
        (display-starting-config simulation-state)
        (in-channel-worker simulation-state)
        (out-channel-worker 8000) ;TODO
        (server-channel-worker)
        (start-status-process)
        (async-persistent-server socket))))
