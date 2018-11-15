(ns poolgp.core
  (:require [poolgp.configuration :as config])
  (:gen-class))

(defn -main
  "Entrypoint"
  [& args]
  (config/start args))
