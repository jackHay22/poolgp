(ns poolgp.peripherals.server
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async]
            [poolgp.simulation.structs :as structs]
            [poolgp.simulation.utils :as utils]
            [poolgp.simulation.manager :as simulation-manager]
            [poolgp.simulation.players.manager :as player-manager]
            [poolgp.log :as log]
            [poolgp.peripherals.monitoring :as monitoring])
  (:import [java.net ServerSocket SocketException Socket InetSocketAddress])
  (:gen-class))

(def IN-CHANNEL (async/chan))
(def OUT-CHANNEL (async/chan))

;count of total individuals in system
(def TOTAL-INDIVS (atom 0))

;Detected ip for return packets
(def REMOTE-HOST (atom nil))

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
         (async/>! IN-CHANNEL (.readLine (io/reader client-socket)))
         (swap! TOTAL-INDIVS inc)
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
          :eval-id (:id p1))
    :p2 (assoc
          (player-manager/init-player (assoc p2 :genetic true) :p2)
          :eval-id (:id p2))))

(defn- run-simulation
  "run the current simulation state
  and output to outgoing channel"
  [starting-state raw-indiv-1 raw-indiv-2]
  (async/go
    (log/write-info (str "Running simulation cycle on individuals: "
                      raw-indiv-1 " | " raw-indiv-2))
    (let [indiv-1 (read-string raw-indiv-1)
          indiv-2 (read-string raw-indiv-2)
          max-cycles (:max-iterations starting-state)
          eval-state (add-players starting-state indiv-1 indiv-2)
          resultant-state
              (loop [current 0
                     state eval-state]
                     (if (> max-cycles current)
                       ;(simulation-manager/simulation-log state)
                       (recur (inc current)
                              (doall (simulation-manager/simulation-update state)))
                     state))]
            (do
              (async/>! OUT-CHANNEL (:p1 resultant-state))
              (async/>! OUT-CHANNEL (:p2 resultant-state))))))

(defn- in-channel-worker
  "start channel worker with starting state"
  [simulation-state]
  (log/write-info "Starting incoming channel worker...")
  (async/go-loop []
    (try
      (run-simulation simulation-state
        (async/<! IN-CHANNEL) (async/<! IN-CHANNEL))
      (catch Exception e
        (log/write-error "Failed to read individual from packet")))
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
        (monitoring/start-monitoring-process)
        (async-persistent-server socket))))
