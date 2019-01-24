(ns poolgp.log
  (:require [poolgp.config :as config])
  (:gen-class))

(def INFO "INFO")
(def ERROR "ERROR")

(def TEXT-RED "\033[0;31m")
(def TEXT-YELLOW "\033[1;33m")
(def TEXT-NC "\033[0m")

;internal
(defn- write
  "write a log message with an optional tag"
      ([msg] (println "poolgp =>" msg))
      ([tag msg] (println "poolgp =>" tag msg))
      ([tag msg color] (println "poolgp =>" color tag TEXT-NC msg)))

(def write-info #(write INFO %))
(def write-error #(if config/TEXT-COLOR?
                      (write ERROR % TEXT-RED)
                      (write ERROR %)))
