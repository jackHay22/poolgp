(ns poolgp.direct
  (:require [poolgp.config :as config]
            [poolgp.simulation.manager :as simulation-manager]
            [poolgp.simulation.players.manager :as player-manager]
            [poolgp.log :as log])
  (:gen-class))

;TODO standard configuration in resources
(def POOLGP-CONFIG-STD "task_definitions/")

(def LOADED-STATE (atom nil))

(defn- create-indiv-return
  "aggregate fitness to return clojush.individual"
  [indiv simulation-states]
  indiv
  ;TODO
  )

(defn- run-simulation
  "run the current simulation state
  and output to outgoing channel"
  [starting-state test-indiv opponent]
  (let [max-cycles (:max-iterations starting-state)
        eval-state (assoc starting-state
                      :p1 (player-manager/init-clojush-player test-indiv :p1)
                      :p2 (player-manager/init-clojush-player opponent   :p2))
        resultant-state
            (loop [current 0
                   state eval-state]
                   (if (> max-cycles current)
                     ;(simulation-manager/simulation-log state)
                     (recur (inc current)
                            (doall (simulation-manager/simulation-update state)))
                   state))]
            ;return individual from state
            (:p1 resultant-state)))

(defn evaluate-individual
   "evaluate a clojush individual against opponents
   using a configuration structure"
   ([indiv opponents task-defn]
     ;load state once
     (if (nil? @LOADED-STATE)
      (reset! LOADED-STATE
        (simulation-manager/simulation-init task-defn false)))
     ;run parallel simulations
     (create-indiv-return
       (doall (pmap (fn [op]
                      (run-simulation @LOADED-STATE indiv op))
                    opponents))))
   ([indiv opponents]
     (evaluate-individual indiv opponents POOLGP-CONFIG-STD)))
