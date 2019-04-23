(ns poolgp.peripherals.tournament
  (:require [poolgp.simulation.utils :as utils]
            [poolgp.simulation.manager :as simulation]
            [poolgp.log :as log])
  (:gen-class))

(defn run-tournament
  "run a tournament for a configured set of individuals"
  [json-config]
  (let [simulation-state (simulation/simulation-init json-config false)]
    (if (:tournament json-config)
      (:entrants (utils/read-json-file (:tournament json-config)))
      (log/write-error
        "The task definition must include a tournament file to use this mode"))))
