(ns poolgp.simulation.eval.server
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async])
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

(defn async-persistent-server
  [socket]

  )

(defn init-server
  "start a persistent socket server"
  [port eval-fn]
  (let [socket (ServerSocket. port)]
    (async-persistent-server socket)
    (println "started server...")))
