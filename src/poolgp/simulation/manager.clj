(ns poolgp.simulation.manager
  (:require [poolgp.simulation.analysis.manager :as analysis-manager]
            [poolgp.simulation.structs :as structs]
            [poolgp.simulation.utils :as utils])
  (:import poolgp.simulation.structs.SimulationState)
  (:gen-class))

(defn simulation-init
  "load task definition from json, parse components recursively
  through structure"
  [task-definition-path]
  (let [json-structure (utils/read-json-file task-definition-path)
        demo? (:demo json-structure)
        simulation-json (:simulation json-structure)]
        (SimulationState.
          ;analysis states
          (analysis-manager/analysis-init (:analysis simulation-json) demo?)
          (:max-iterations simulation-json)
          0
          (:port simulation-json))))

(defn simulation-update
  "update transform on simulation state"
  [state]
  (update-in state [:analysis-states]
    #(map analysis-manager/analysis-update %)))

(defn simulation-render
  "take simulation state and optionally
  Graphics2D context (demo mode)"
  [state gr]
  ; (doall
  ;   (map
  ;       #(analysis-manager/analysis-render % gr)
  ;       (:analysis-states state))))
  (analysis-manager/analysis-render (first (:analysis-states state)) gr))
