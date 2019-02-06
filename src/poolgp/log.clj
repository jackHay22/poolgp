(ns poolgp.log
  (:require [poolgp.config :as config])
  (:import java.text.SimpleDateFormat)
  (:import java.util.Date)
  (:gen-class))

(def INFO "INFO")
(def ERROR "ERROR")
(def WARNING "WARNING")

(def TEXT-RED "\033[0;31m")
(def TEXT-YELLOW "\033[1;33m")
(def TEXT-NC "\033[0m")

(defn- get-timestamp
   []
   (.format (SimpleDateFormat. "'['MM-dd-yyyy HH:mm.ss']'") (Date.)))

;internal
(defn- write
  "write a log message with an optional tag"
      ([msg] (println "poolgp" (get-timestamp) msg))
      ([tag msg] (write (str tag ": " msg)))
      ([tag msg color] (write (str color tag TEXT-NC ": " msg))))

(def write-info #(write INFO %))
(def write-error #(if config/TEXT-COLOR?
                      (write ERROR % TEXT-RED)
                      (write ERROR %)))
(def write-warning #(if config/TEXT-COLOR?
                      (write WARNING % TEXT-YELLOW)
                      (write WARNING %)))
