(ns poolgp.simulation.manager
  (:require [poolgp.simulation.analysis.manager :as analysis-manager]
            [poolgp.simulation.resources :as resources]
            [poolgp.simulation.structs :as structs]
            [poolgp.simulation.utils :as utils]
            [poolgp.config :as config]
            [poolgp.log :as log]
            [poolgp.simulation.players.manager :as player-manager])
  (:import poolgp.simulation.structs.SimulationState)
  (:import clojush.individual.individual)
  (:gen-class))

(defn simulation-init
  "load task definition from json, parse components recursively
  through structure"
  [task-definition demo?]
  (let [simulation-json (:simulation task-definition)]
        (SimulationState.
          ;analysis states
          (analysis-manager/analysis-init
            (:analysis simulation-json) demo?)
          ;supports a default value if not included
          (if (:max-iterations simulation-json)
              (:max-iterations simulation-json) config/DEFAULT-MAX-ITERATIONS)
          0 ;current iteration
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

(defn calculate-individual-fitness
  "takes individual and completed states, generates fitness and
  returns individual"
  [pgp-indiv final-simulation-states]
  (assoc-in
    pgp-indiv [:indiv :errors]
    (into []
      (reduce (fn [errors sim-state]
                (let [analysis-states (:analysis-states sim-state)]
                  (concat errors
                    (mapcat (fn [a-state]
                              (let [p1-analytics (:p1-analytics a-state)]
                                  (list
                                    ;TODO
                                    ;TODO: count balls instead?
                                    (- 8 (:score p1-analytics))
                                    (:scratches p1-analytics)
                                    )))
                         analysis-states))))
              (list) final-simulation-states))))
