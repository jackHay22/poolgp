(ns poolgp.simulation.analysis.manager
  (:require [poolgp.simulation.analysis.game.manager :as game-manager]
            [poolgp.config :as config]
            [poolgp.simulation.analysis.definitions :as analytics-defs])
  (:import poolgp.simulation.structs.AnalysisState)
  (:import poolgp.simulation.structs.PlayerAnalytics)
  (:gen-class))

(def ANALYTICS (list analytics-defs/score
                     analytics-defs/balls-remaining))

(defn analysis-init
  "initialize gamestate and analytics"
  [analysis-vec-json images?]
  (doall
    (map
      #(AnalysisState.
        ;game state
        (game-manager/game-init (:game %) images?)
        (PlayerAnalytics. {:score 0 :total nil} config/ZERO-SCORE-PENALTY)
        (PlayerAnalytics. {:score 0 :total nil} config/ZERO-SCORE-PENALTY))
    analysis-vec-json)))

(defn analysis-update
  "update analytics based on current gamestate"
  [analysis-state]
  (let [game-updated-state (update-in analysis-state
                              [:game-state] game-manager/game-update)]
    (if (:ready? (:game-state game-updated-state))
      (let [update-fn #(reduce (fn [state transform]
                                  (transform
                                      (:game-state game-updated-state) state %2))
                              %1 ANALYTICS)]
          (update-in
            (update-in
              game-updated-state
              [:p1-analytics] update-fn :p1)
              [:p2-analytics] update-fn :p2))
      game-updated-state)))

(defn analysis-render
  "display gamestate and display analytics"
  [analysis-state gr]
  (game-manager/game-render
    (:game-state analysis-state) gr))
