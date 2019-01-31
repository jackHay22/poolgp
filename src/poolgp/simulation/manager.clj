(ns poolgp.simulation.manager
  (:require [poolgp.simulation.analysis.manager :as analysis-manager]
            [poolgp.simulation.resources :as resources]
            [poolgp.simulation.structs :as structs]
            [poolgp.simulation.utils :as utils]
            [poolgp.config :as config]
            [poolgp.log :as log]
            [poolgp.simulation.players.manager :as player-manager])
  (:import poolgp.simulation.structs.SimulationState)
  (:gen-class))

(defn simulation-init
  "load task definition from json, parse components recursively
  through structure"
  [task-definition-path demo?]
  (let [simulation-json (:simulation (utils/read-json-file task-definition-path))]
        (SimulationState.
          ;analysis states
          (analysis-manager/analysis-init
            (:analysis simulation-json) demo?)
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
          (if demo?
            (update-in resources/CONTROLLER
                    [:cue] utils/load-image)
            nil))))

(defn simulation-update
  "update transform on simulation state"
  [state]
  (update-in
    (update-in state
      [:analysis-states]
        #(doall (map (fn [a-state]
          ;perform analysis updates
                (analysis-manager/analysis-update
                  (update-in a-state [:game-state]
                    (fn [gs]
                      ;update gamestate with current player
                      (player-manager/update-operations
                        gs ((:id (:current gs)) state)
                           (:controller state)))))) %)))
      ;update current iteration
      [:current-iteration] inc))

(defn simulation-render
  "take simulation state and optionally
  Graphics2D context (demo mode)"
  [state gr]
  (if (> (count (:analysis-states state)) 0)
      ;render to window
      (let [display-state (nth (:analysis-states state)
            (min (:watching state) (count (:analysis-states state))))]
            (analysis-manager/analysis-render display-state gr)
            ;render controller (demo)
            (player-manager/display-operations
                        gr (:game-state display-state) (:controller state)))
    ;display default notification (no analysis states)
    (doto gr
      (.setColor config/PANEL-INFO-COLOR)
      (.setFont config/PANEL-SCORE-FONT)
      (.drawString "No games configured"
        (- (int (/ config/WINDOW-WIDTH-PX 2)) 120)
        400))))

(defn simulation-log
  "write simulation logs"
  [state]
  (if (> (count (:analysis-states state)) 0)
      (doall (map #(analysis-manager/analysis-log %)
                   (:analysis-states state)))
      (log/write-error "No games configured in task definition")))
