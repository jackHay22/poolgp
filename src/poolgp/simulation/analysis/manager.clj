(ns poolgp.simulation.analysis.manager
  (:require [poolgp.simulation.analysis.game.manager :as game-manager]
            [poolgp.simulation.analysis.analytics :as analytics])
  (:import poolgp.simulation.structs.AnalysisState)
  (:gen-class))

(defn analysis-init
  "initialize gamestate and analytics"
  [analysis-vec-json images?]
  (doall
    (map
      #(AnalysisState.
        ;game state
        (game-manager/game-init (:game %) images?)
        (:p1-analytics %)
        (:p2-analytics %))
    analysis-vec-json)))

(defn analysis-update
  "take analytics state, update gamestate and
  perform analytics measurements"
  [analysis-state]
  (analytics/update-analytics
    (update-in analysis-state
      [:game-state] game-manager/game-update)))

(defn analysis-render
  "display gamestate and display analytics"
  [analysis-state gr]
  (do
    (game-manager/game-render (:game-state analysis-state) gr)
    (analytics/report-analytics analysis-state gr)))
