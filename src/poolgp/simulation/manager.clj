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
          (or (:max-iterations simulation-json)
              config/DEFAULT-MAX-ITERATIONS)
          0 ;current iteration
          (or (:watching simulation-json) 0)
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
              (if (not (:game-complete? (:game-state a-state)))
                (analysis-manager/analysis-update
                  (update-in a-state [:game-state]
                    (fn [gs]
                      ;update gamestate with current player
                      (player-manager/update-operations
                        gs ((:id (:current gs)) state)
                           (:controller state)))))
                a-state)) %)))
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

(defn calculate-individual-fitness
  "takes individual and completed states, generates fitness and
  returns individual"
  [pgp-indiv final-simulation-states]
  (let [score-or-penalty #(- (or (:total %)
                                 config/ZERO-SCORE-PENALTY)
                              (:score %))]
      (assoc
        pgp-indiv :errors
        {:self (reduce (fn [errors sim-state]
                          (reduce #(conj %1
                                      ;own balls remaining
                                      (or
                                        (:balls-remaining (:p1-analytics %2))
                                        ;penalty for no ball assignment
                                        config/ZERO-SCORE-PENALTY)
                                      ;opponent balls remaining
                                      ; (- 8
                                      ;   (or
                                      ;     (:balls-remaining (:p2-analytics %2))
                                      ;     ;If player not even assigned a ball, zero error
                                      ;     8))
                                          )
                            errors (:analysis-states sim-state)))
                        [] final-simulation-states)
         ;extract opponent scores and create lookup
         :opp (reduce (fn [opp-map sim-state]
                        (assoc opp-map
                          (keyword (str (:uuid (:clojush-indiv (:p2 sim-state)))))
                          (reduce #(conj %1
                                      ;opponent balls remaining
                                      (or
                                        (:balls-remaining (:p2-analytics %2))
                                        config/ZERO-SCORE-PENALTY)
                                      ;own balls remaining
                                      ; (- 8
                                      ;   (or
                                      ;     (:balls-remaining (:p1-analytics %2))
                                      ;     8))
                                          )
                              [] (:analysis-states sim-state))))
              (hash-map) final-simulation-states)})))
