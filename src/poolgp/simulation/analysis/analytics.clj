(ns poolgp.simulation.analysis.analytics
  (:require [poolgp.simulation.utils :as utils]
            [poolgp.simulation.analysis.definitions :as analytics-defs]
            [poolgp.log :as log])
  (:gen-class))

(def ANALYTICS-NS "poolgp.simulation.analysis.definitions/")

(defn- resolve-loaded-name
  "resolve action to qualified function name"
  [function-name family-marker]
  (ns-resolve *ns*
    (symbol (str ANALYTICS-NS function-name family-marker))))

(defn load-analytics
  "load analytics"
  [analytics-list player-id]
  (map #((resolve-loaded-name % "_") player-id) analytics-list))

(defn update-analytics
  "update analytics based on current gamestate"
  [analysis-state]
  (let [update-fn #(analytics-defs/update-analytic %
                      (:game-state analysis-state))]
    (utils/updates-in analysis-state
      [:p1-analytics] #(map update-fn %)
      [:p2-analytics] #(map update-fn %))))

(defn report-analytics
  "log any data about analytics state"
  [s]
  (do
    (log/write-info "-- Player p1 analytics --")
    (doall (map analytics-defs/display
                (:p1-analytics s)))
    (log/write-info "-- Player p2 analytics --")
    (doall (map analytics-defs/display
                (:p2-analytics s)))))
