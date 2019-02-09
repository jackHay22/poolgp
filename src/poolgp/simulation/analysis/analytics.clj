(ns poolgp.simulation.analysis.analytics
  (:require [poolgp.simulation.utils :as utils]
            [poolgp.simulation.analysis.definitions :as analytics-defs]
            [poolgp.log :as log])
  (:import poolgp.simulation.structs.PlayerAnalytics)
  (:gen-class))

(def ANALYTICS (list analytics-defs/score
                     analytics-defs/forward-movement
                     analytics-defs/scratches
                     analytics-defs/scored-turns))

(defn state [] (PlayerAnalytics. 0 0 0 0))

(defn update-analytics
  "update analytics based on current gamestate"
  [analysis-state]
  (if (:ready? (:game-state analysis-state))
    (let [update-fn #(reduce (fn [state transform]
                                (transform
                                    (:game-state analysis-state) state %2))
                            %1 ANALYTICS)]
        (update-in
          (update-in analysis-state
            [:p1-analytics] update-fn :p1)
            [:p2-analytics] update-fn :p2))
    analysis-state))

(defn report-analytics
  "log any data about analytics state"
  [s]
)
