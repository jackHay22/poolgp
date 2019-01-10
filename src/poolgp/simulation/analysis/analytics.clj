(ns poolgp.simulation.analysis.analytics
  (:require [poolgp.simulation.utils :as utils]
            [poolgp.simulation.structs :as structs])
  (:gen-class))

(defn update-analytics
  "update analytics based on current gamestate"
  [analysis-state]
  (let [update-fn #(structs/analyze % (:game-state analysis-state))]
    (utils/updates-in analysis-state
      [:p1-analytics] #(map update-fn %)
      [:p2-analytics] #(map update-fn %))))

(defn report-analytics
  [analysis-state gr]
  )
