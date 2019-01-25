(ns poolgp.simulation.analysis.definitions
  (:require [poolgp.log :as log])
  (:gen-class))

(defn- get-player
  "get player from gamestate by id"
  [gs id]
  (if (= (:id (:current gs)) id)
      (:current gs) (:waiting gs)))

(defprotocol Analyze
  (update-analytic [a s])
  (display [a]))

;operation: (fn [current-val game-state]) -> value
;aggregate: (fn [analytic]) -> int
(defrecord Analytic [name operation value aggregate]
      Analyze
        (update-analytic [a s]
          (update-in a [:value] (:operation a) s))
        (display [a]
          (log/write-info
            (str "  -> Analytic \"" (:name a)
                 "\" value:" (:value a)))))

; ----------- ANALYTICS DEFINITIONS -----------
(defn score_ [id]
  (Analytic. "score" #(max %1 (:score (get-player %2 id)))
                    0 (fn [a] (:value a))))

(defn scored_turns_ [id]
  (Analytic. (str "scored_turns" id)
    (fn [current-val a-state]
      ;TODO
      ) {:total 0} (fn [a] (:total (:value a)))))

(defn scratches_ [id]
  (Analytic. (str "scratches" id)
    (fn [current-val a-state]
      ;TODO
      ) {:total 0} (fn [a] (:total (:value a)))))
