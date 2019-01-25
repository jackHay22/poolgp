(ns poolgp.peripherals.server
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async]
            [poolgp.simulation.structs :as structs]
            [poolgp.simulation.manager :as simulation-manager]
            [poolgp.log :as log])
  (:import [java.net ServerSocket SocketException])
  (:gen-class))

(def RUNNING? (atom true))

(def IN-CHANNEL (async/chan))
(def OUT-CHANNEL (async/chan))

; (defn persistent-server
;   "persistent async TCP server"
;   [socket eval-hook]
;   (future
;     (while @RUNNING?
;       (with-open [server (.accept socket)]
;         (let [new-data (.readLine (io/reader server))
;               tested (eval-hook (read-string new-data))
;               processed (pr-str tested)
;               writer (io/writer server)]
;               (.write writer processed)
;               (.flush writer))))) RUNNING?)

; (defn async-persistent-server
;   [socket]
;   (future
;     (with-open [server (.accept socket)]
;       (async/go-loop []
;        (if (and (not (.isClosed socket)) (.isBound socket))
;          (try
;            (println "pushing to channel...")
;            (async/>! IN-CHANNEL (.readLine (io/reader server)))
;            (catch SocketException e
;              (.close server)
;              (async/close! IN-CHANNEL)
;              (async/close! OUT-CHANNEL))))))))

(defn display-starting-config
  "log starting configuration state"
  [s]
  (doall (map log/write-info
      (list
        (str "Port: " (:port s))
        (str "Max iterations: " (:max-iterations s))
        (str "Total analysis-states: " (count (:analysis-states s)))))))

(defn start-server
  "start a persistent socket server"
  [task-def]
  (let [simulation-state (simulation-manager/simulation-init task-def false)
        socket (ServerSocket. (:port simulation-state))]
        (display-starting-config simulation-state)
        (log/write-info "Starting persistent async server")
        ))
