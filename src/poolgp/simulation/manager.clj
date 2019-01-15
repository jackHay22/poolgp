(ns poolgp.simulation.manager
  (:require [poolgp.simulation.analysis.manager :as analysis-manager]
            [poolgp.simulation.structs :as structs]
            [poolgp.simulation.utils :as utils]
            [poolgp.config :as config]
            [poolgp.simulation.players.manager :as player-manager])
  (:import poolgp.simulation.structs.SimulationState)
  (:gen-class))

(defn simulation-init
  "load task definition from json, parse components recursively
  through structure"
  [task-definition-path]
  (let [json-structure (utils/read-json-file task-definition-path)
        simulation-json (:simulation json-structure)]
        (SimulationState.
          ;analysis states
          (analysis-manager/analysis-init
            (:analysis simulation-json) (:demo simulation-json))
          ;supports a default value if not included
          (if (:max-iterations simulation-json)
              (:max-iterations simulation-json) config/DEFAULT-MAX-ITERATIONS)
          0 ;current iteration
          (if (:port simulation-json)
              (:port simulation-json) config/DEFAULT-PORT)
          (if (:watching simulation-json)
              (:watching simulation-json) 0)
          (player-manager/init-player (:p1 simulation-json) :p1)
          (player-manager/init-player (:p2 simulation-json) :p2)
          (:demo simulation-json))))

(defn simulation-update
  "update transform on simulation state"
  [state]
  ;TODO: enforce max iterations
  (update-in
    (update-in state [:analysis-states]
        #(map (fn [a-state]
                (analysis-manager/analysis-update
                  (update-in a-state [:game-state]
                    (fn [gs]
                      (player-manager/update-operations
                        gs ((:current gs) state)))))) %))
      [:current-iteration] inc))

(defn simulation-render
  "take simulation state and optionally
  Graphics2D context (demo mode)"
  [state gr]
  (analysis-manager/analysis-render
    (nth (:analysis-states state)
          (min (:watching state) (count (:analysis-states state))))
    gr (:demo state)))
