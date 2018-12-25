(ns poolgp.core
  (:require [poolgp.manager :as manager])
  (:gen-class))

(defn -main
  "Entrypoint"
  [& args]
  (manager/start args))
