(ns poolgp.log
  (:gen-class))

(def INFO "INFO")
(def ERROR "ERROR")

(defn write
  "write a log message with an optional tag"
      ([msg] (println "poolgp =>" msg))
      ([tag msg] (println "poolgp =>" tag msg)))

(def write-info #(write INFO %))
(def write-error #(write ERROR %))
