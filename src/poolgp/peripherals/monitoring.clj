(ns poolgp.peripherals.monitoring
  (:require [poolgp.log :as log])
  (:import java.net.DatagramSocket)
  (:import java.net.InetAddress)
  (:gen-class))

(defn- status-task
  "runnable (thread) log process"
  []
  (loop []
    (do
      (log/write-info (str "Active threads: " (Thread/activeCount)))
      ;(log/write-info (str "Total memory: " (.totalMemory (Runtime/getRuntime))))
      ;sleep for ten seconds
      (Thread/sleep 10000))
      (recur)))

(defn start-monitoring-process
  "start a background thread that periodically logs"
  []
  (log/write-info "Starting monitoring background task...")
  (.start (Thread. status-task)))
