(ns poolgp.simulation.analysis.analytics
  (:require [poolgp.simulation.utils :as utils])
  (:gen-class))

(def ANALYTICS-NS "poolgp.simulation.analysis.analytics/")
(defprotocol Analyze (update-analytic [a s]))

;operation: (fn [current-val analysis-state]) -> value
(defrecord Analytic [operation value]
      Analyze
        (update-analytic [a s]
          (update-in a [:value] (:operation a) s)))

(defn- resolve-loaded-name
  "resolve action to qualified function name"
  [function-name family-marker]
  (ns-resolve *ns*
    (symbol (str ANALYTICS-NS function-name family-marker))))

;test
(defn score_ []
  (Analytic. #(max %1 (:p1-score %2)) 0))

(defn load-analytics
  "load analytics"
  [analytics-list]
  (map #((resolve-loaded-name % "_")) analytics-list))

(defn update-analytics
  "update analytics based on current gamestate"
  [analysis-state]
  (let [update-fn #(update-analytic % (:game-state analysis-state))]
    (utils/updates-in analysis-state
      [:p1-analytics] #(map update-fn %)
      [:p2-analytics] #(map update-fn %))))

(defn report-analytics
  [analysis-state gr])
