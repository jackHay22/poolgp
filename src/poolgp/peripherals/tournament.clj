(ns poolgp.peripherals.tournament
  (:require [poolgp.simulation.utils :as utils]
            [poolgp.simulation.manager :as simulation]
            [poolgp.simulation.players.manager :as player-manager]
            [poolgp.log :as log])
  (:gen-class))

(defn- calc-pts
  "takes final simulation state and
  returns 1 or 0 or 0.5 for tie"
  [final-state]
  ;TODO
  1
  )

(defn- write-tournament-report
  "takes final results set"
  [results]
  (do
    (log/write-info "Best 3 individuals from tournament:")
    (doall
      (map #(log/write-info
              (str (+ %2 1) ". ID: " (:id %1) ", WINS: " (:wins %1)))
        (take 3
          (sort-by :wins results))
        (range)))))

(defn run-tournament
  "run a tournament for a configured set of individuals"
  [json-config]
  (let [simulation-state (simulation/simulation-init json-config false)
        max-cycles (:max-iterations simulation-state)]
    (if (:tournament json-config)
      ;run complete tournament
      (write-tournament-report
        (let [indiv-pool (:entrants (utils/read-json-file (:tournament json-config)))]
            (map (fn [i1]
                (assoc i1 :wins
                  ;sum game results for individual
                  (reduce +
                    (map (fn [i2]
                          ;get pts from final state
                          (calc-pts
                            (loop [current 0
                                   state (assoc simulation-state
                                              :p1 (player-manager/init-clojush-player i1 :p1)
                                              :p2 (player-manager/init-clojush-player i2 :p2))]
                                   (if (> max-cycles current)
                                     ;TODO: calculate fitness
                                     (recur (inc current)
                                            (doall (simulation/simulation-update state)))
                                   state))))
                         indiv-pool))))
                indiv-pool)))
      (log/write-error
        "The task definition must include a tournament file to use this mode"))))
