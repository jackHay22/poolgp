(ns poolgp.evalserver.server
  (:require [clojure.java.io :as io])
  (:import [java.net ServerSocket])
  (:gen-class))

(def RUNNING? (atom true))

;utilities
(def close-socket (fn [socket] (.close socket)))
(def socket-closed? (fn [socket] (.isClosed socket)))

(defn persistent-server
    "persistent async TCP server to communicate with network load balancer"
    [socket config]
    (let [manager (manager/send-to-test config)]
      (future
        (while @running
          ;get data, process in testbench, send back
          (with-open [server (.accept socket)]
            (let [new-data (.readLine (io/reader server))
                  tested (manager (read-string new-data))
                  processed (pr-str tested)
                  writer (io/writer server)]
                  (.write writer processed)
                  (.flush writer))))) running))

(defn start-server
    "start and accept a connection to a tcp socket server"
    [config] (let [socket (ServerSocket. (:port config))]
        ;use socket to create async persistent server
        (do ((:send-log config) "starting_node")
            (persistent-server socket config)
            socket)))

(defn init-server
  "start a listening server on port"
  [port])
