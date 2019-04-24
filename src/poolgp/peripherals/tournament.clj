(ns poolgp.peripherals.tournament
  (:require [poolgp.simulation.utils :as utils]
            [poolgp.config :as config]
            [poolgp.simulation.manager :as simulation]
            [poolgp.simulation.players.manager :as player-manager]
            [poolgp.log :as log])
  (:gen-class))

(def load-cache (memoize player-manager/init-player))

(defn- calc-pts
  "takes final simulation state and
  returns 1 or 0"
  [final-simulation-state]
  (reduce
    #(let [own-remaining
            (or
              (:balls-remaining (:p1-analytics %2))
                  ;penalty for no ball assignment
                  config/ZERO-SCORE-PENALTY)
           opp-remaining
            (or
              (:balls-remaining (:p2-analytics %2))
                  ;penalty for no ball assignment
                  config/ZERO-SCORE-PENALTY)]
            (+ %1
              (cond
                (< own-remaining opp-remaining) 3
                (> own-remaining opp-remaining) 0
                :tie 1)))
      0 (:analysis-states final-simulation-state)))

(defn- write-tournament-report
  "takes final results set"
  [results show-best]
  (do
    (log/write-info "Tournament Results:")
    (doall
      (map #(log/write-info
              (str (+ %2 1) ". ID: " (:id %1) ", POINTS: " (:pts %1)))
          (reverse (sort-by :pts results))
        (range)))
    (System/exit 0)))

(defn run-tournament
  "run a tournament for a configured set of individuals"
  [json-config]
  (let [simulation-state (simulation/simulation-init json-config false)
        max-cycles (:max-iterations simulation-state)]
    (log/write-info "Running tournament...")
    (if (:tournament json-config)
      ;run complete tournament
      (write-tournament-report
        (let [indiv-pool (:entrants (utils/read-json-file (:tournament json-config)))]
            (doall
              (map (fn [i1]
                (assoc i1 :pts
                  ;sum game results for individual
                  (reduce +
                    (doall (pmap (fn [i2]
                          ;get pts from final state
                          (if (not (= (:id i1) (:id i2)))
                            (calc-pts
                              (loop [current 0
                                     state (assoc simulation-state
                                              :p1 (load-cache i1 :p1)
                                              :p2 (load-cache i2 :p2))]
                                     (if (> max-cycles current)
                                         (recur (inc current)
                                                (simulation/simulation-update state))
                                         state)))
                              0))
                         indiv-pool)))))
                indiv-pool)))
            ;display best 3 individuals
            3)
      (log/write-error
        "The task definition must include a tournament file to use this mode"))))
