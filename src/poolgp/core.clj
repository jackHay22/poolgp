(ns poolgp.core
  (:require [poolgp.manager :as manager])
  (:gen-class))

(defn -main [& args] (manager/start args))
