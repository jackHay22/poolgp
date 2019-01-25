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
        (analytics/load-analytics (:p1-analytics %) :p1)
        (analytics/load-analytics (:p2-analytics %) :p2))
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
  (game-manager/game-render
    (:game-state analysis-state) gr))

(defn analysis-log
  "write analysis logs"
  [analysis-state]
  (analytics/report-analytics analysis-state))
