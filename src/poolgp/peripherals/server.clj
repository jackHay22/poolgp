(ns poolgp.peripherals.server
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async]
            [poolgp.simulation.structs :as structs]
            [poolgp.simulation.utils :as utils]
            [poolgp.simulation.manager :as simulation-manager]
            [poolgp.simulation.players.manager :as player-manager]
            [poolgp.config :as config]
            [poolgp.log :as log])
  (:import [java.net ServerSocket SocketException Socket InetAddress])
  (:import poolgp.simulation.structs.ServerConfig)
  (:import clojush.individual.individual)
  (:gen-class))

;holds individuals from engine, pulled off individually
(def IN-CHANNEL (async/chan config/CHANNEL-BUFFER))

;holds individuals that have already been tested
(def OUT-CHANNEL (async/chan config/CHANNEL-BUFFER))

;Holds individuals being used as tests for individuals in test mode
(def OPPONENT-POOL (atom (list)))

;Number Received individuals per generation
(def INDIV-COUNT (atom 0))

;This is the current evaluation cycle: if a new cycle is detected the opponent pool is purged
(def CURRENT-CYCLE (atom 0))

(defn- valid-indiv?
  "takes individual, checks if valid
  -> bool"
  [i]
  (and
    (map? i)
    (instance? individual (:indiv i))))

(defn- load-config
  "create server config record from json->map"
  [task-def]
  (ServerConfig.
    (:indiv-ingress-p task-def)
    (:indiv-egress-p task-def)
    (:opp-pool-req-p task-def)
    (:engine-hostname task-def)))

(defn- request-opponent-pool!
  "request opponent pool from remote engine
  (blocking), resets! OPPONENT-POOL"
  [hostname req-p]
  (let [client-socket (Socket. hostname req-p)
        reader (io/reader client-socket)]
      (log/write-info (str "Requesting opponent pool from: "
                            hostname ":" req-p))
      (reset! OPPONENT-POOL
        (filter valid-indiv?
          (map read-string (line-seq reader))))))

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
          (log/write-info (str "Simulation started on " @INDIV-COUNT " individuals"))
          (Thread/sleep delay))
          (recur)))))

(defn- async-persistent-server
  "start listening server, push individuals to channel"
  [socket]
  (log/write-info "Starting persistent async server...")
  ;continues on main thread
  (async/go-loop []
    (let [client-socket (.accept socket)]
     (try
       (let [line-from-sock (.readLine (io/reader client-socket))]
          ;verify that line can be placed in channel
          (if (not (nil? line-from-sock))
              (async/>! IN-CHANNEL line-from-sock)
              (log/write-warning "Ingress server read nil line")))
       (.close client-socket)
       (catch Exception e
         (.close client-socket)
         (log/write-error "Exception moving packet to channel, closing current conn")
         (.printStackTrace e))))
    (recur)))

(defn- run-simulation
  "run the current simulation state
  and output to outgoing channel"
  [starting-state test-indiv opponent]
  (let [max-cycles (:max-iterations starting-state)
        eval-state (assoc starting-state
                      :p1 (player-manager/init-clojush-player test-indiv :p1)
                      :p2 (player-manager/init-clojush-player opponent   :p2))
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
  "take resulting state and turn into report to return to engine"
  [indiv results-state]
  ;TODO: incorporate results list
  ;TODO: check if results list is empty (this can happen on a opp pool clear)
  indiv
  ;using :clojush-indiv
  )

(defn- in-channel-worker
  "start channel worker with starting state"
  [simulation-state server-config]
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
          (do
            (swap! INDIV-COUNT inc)
            ;if node hasn't requested opponents for this cycle,
            ; request from engine host (block)
            (if (empty? @OPPONENT-POOL)
              (request-opponent-pool!
                (:engine-hostname server-config)
                (:opp-pool-req-p server-config)))
            (log/write-info (str "Running simulations on individual "
                                  (:eval-id indiv) " against " (count @OPPONENT-POOL)
                                  " opponents"))
            ;validate individual before starting simulation
            (if (valid-indiv? indiv)
              (async/>! OUT-CHANNEL
                ;create return map
                (create-outgoing-map indiv
                  (doall ((if config/PARALLEL-SIMULATIONS? pmap map)
                        (fn [op]
                          (run-simulation simulation-state indiv op))
                        @OPPONENT-POOL))))
              (log/write-error (str "Failed to validate individual: " indiv))))))
      (catch Exception e
        (log/write-error "In channel worked failed to evaluate individual on opponent pool (Exception)")
        (.printStackTrace e)))
    (recur)))

(defn- out-channel-worker
  "start a distribution worker"
  [engine-hostname port]
  (log/write-info "Starting outgoing channel worker...")
  (async/go-loop []
    (let [player (async/<! OUT-CHANNEL)
          client-socket (Socket. engine-hostname port)
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
        (str "Host: " (.getHostName (InetAddress/getLocalHost)))
        (str "Total system cores: " (.availableProcessors (Runtime/getRuntime)))
        (str "Channel buffer size: " config/CHANNEL-BUFFER)
        (str "Using pmap for simulations? " config/PARALLEL-SIMULATIONS?)
        (str "Logging eval server status every " config/LOG-SPACING-SECONDS " seconds")
        (str "Max iterations: " (:max-iterations s))
        (str "Total analysis-states: " (count (:analysis-states s)))))))

(defn start-server
  "start a persistent socket server"
  [task-def]
  (let [simulation-state (simulation-manager/simulation-init task-def false)
        server-config (load-config (:eval-worker task-def))
        socket (ServerSocket. (:indiv-ingress-p server-config))]
      (do
        (.setSoTimeout socket 0)
        (display-starting-config simulation-state)
        (in-channel-worker simulation-state server-config)
        (out-channel-worker
          (:engine-hostname server-config)
          (:indiv-egress-p server-config))
        (status-task)
        (async-persistent-server socket))))
