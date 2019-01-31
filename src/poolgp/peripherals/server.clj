(ns poolgp.peripherals.server
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async]
            [poolgp.simulation.structs :as structs]
            [poolgp.simulation.utils :as utils]
            [poolgp.simulation.manager :as simulation-manager]
            [poolgp.simulation.players.manager :as player-manager]
            [poolgp.config :as config]
            [poolgp.log :as log])
  (:import [java.net ServerSocket SocketException Socket InetSocketAddress])
  (:gen-class))

;holds individuals from engine, pulled off individually
(def IN-CHANNEL (async/chan config/CHANNEL-BUFFER))

;holds individuals that have already been tested
(def OUT-CHANNEL (async/chan config/CHANNEL-BUFFER))

;Detected ip for return packets
(def REMOTE-HOST (atom nil))

;Holds individuals being used as tests for individuals in test mode
(def OPPONENT-POOL (atom (list)))

;Number Received individuals per generation
(def INDIV-COUNT (atom 0))

;This is the current evaluation cycle: if a new cycle is detected the opponent pool is purged
(def CURRENT-CYCLE (atom 0))

(defn- status-task
  "runnable (thread) log process"
  []
  (let [delay (* config/LOG-SPACING-SECONDS 1000)]
    (future
      (loop []
        (do
          (log/write-info (str "Active threads: " (Thread/activeCount)))
          (log/write-info (str "Opponent pool size: " (count @OPPONENT-POOL)))
          (log/write-info (str "Current cycle: " @CURRENT-CYCLE))
          (log/write-info (str "Simulation started on individuals: " @INDIV-COUNT))
          (Thread/sleep delay))
          (recur)))))

(defn- get-total-cores
  "returns number of processing cores"
  [] (.availableProcessors (Runtime/getRuntime)))

(defn- async-persistent-server
  "start listening server, push individuals to channel"
  [socket]
  (log/write-info "Starting persistent async server...")
  ;continues on main thread
  (async/go-loop []
    (let [client-socket (.accept socket)]
     (try
       (if (nil? @REMOTE-HOST)
            (let [return-addr (.getHostName (.getInetAddress client-socket))]
              (log/write-info (str "Returning individuals to: " return-addr))
              (reset! REMOTE-HOST return-addr)))
        (async/>! IN-CHANNEL (.readLine (io/reader client-socket)))
       (.close client-socket)
       (catch SocketException e
         (.close client-socket)
         (log/write-error "SocketServer exception, closing current conn"))))
    (recur)))

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
            (:p1 resultant-state)))

(defn- create-outgoing-map
  "take resulting gamestates and turn into report to return to engine"
  [indiv results-list]
  ;TODO: incorporate results list
  ;TODO: check if results list is empty (this can happen on a opp pool clear)
  indiv
  )

(defn- in-channel-worker
  "start channel worker with starting state"
  [simulation-state]
  (log/write-info "Starting incoming channel worker...")
  (async/go-loop []
    (try
      (let [indiv (read-string (async/<! IN-CHANNEL))]
        (do
          ;check if current cycle has changed
          (if (not (= (:cycle indiv) @CURRENT-CYCLE))
             (do
               (log/write-info "Detected new cycle, clearing opponent pool")
               (reset! OPPONENT-POOL (list))
               (reset! INDIV-COUNT 0)
               (reset! CURRENT-CYCLE (:cycle indiv))))
          (if (= (:type indiv) :opponent)
            ;add opponent to pool
            (swap! OPPONENT-POOL conj indiv)
            ;simulate on individual
            (do
              (swap! INDIV-COUNT inc)
              (log/write-info (str "Running simulations on individual "
                                    (:eval-id indiv) " against " (count @OPPONENT-POOL)
                                    " opponents"))
              (async/>! OUT-CHANNEL
                (create-outgoing-map indiv
                  (doall ((if config/PARALLEL-SIMULATIONS? pmap map)
                        (fn [op]
                          (run-simulation simulation-state indiv op))
                        @OPPONENT-POOL))))))))
      (catch Exception e
        (log/write-error "In channel worked failed to evaluate individual on opponent pool")
        (.printStackTrace e)))
    (recur)))

(defn- out-channel-worker
  "start a distribution worker"
  [port]
  (log/write-info "Starting outgoing channel worker...")
  (async/go-loop []
    (let [player (async/<! OUT-CHANNEL)
          client-socket (Socket. @REMOTE-HOST port)
          writer (io/writer client-socket)]
        (log/write-info (str "Finished simulation cycle on individual: " (:eval-id player)))
        (.write writer (str (pr-str player) "\n"))
        (.flush writer)
        (.close client-socket))
    (recur)))

(defn- display-starting-config
  "log starting configuration state"
  [s]
  (doall (map log/write-info
      (list
        (str "Listening on port: " (:port s))
        (str "Total system cores: " (get-total-cores))
        (str "Channel buffer size: " config/CHANNEL-BUFFER)
        (str "Using pmap for simulations? " config/PARALLEL-SIMULATIONS?)
        (str "Logging eval server status every " config/LOG-SPACING-SECONDS " seconds")
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
        (out-channel-worker 8001) ;TODO (add to config)
        (status-task)
        (async-persistent-server socket))))
