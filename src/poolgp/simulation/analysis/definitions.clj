(ns poolgp.simulation.analysis.definitions
  (:require [poolgp.log :as log])
  (:gen-class))

(defn- get-player
  "get player from gamestate by id"
  [gs id]
  (if (= (:id (:current gs)) id)
      (:current gs) (:waiting gs)))

(defn- player-is-current?
  "check if player is current player"
  [gs id]
  (= id (:id (:current gs))))

(defn- on-current-unrecorded
  [gs current-val id]
  ;TODO: need to simplify analytic internal code
  )

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
  (Analytic. (str "score/" id)
             #(max %1 (:score (get-player %2 id)))
             0 (fn [a] (:value a))))

(defn scored_turns_ [id]
  (Analytic. (str "scored_turns/" id)
    (fn [current-val g-state]
      (if (and (player-is-current? g-state id)
               (not (:recorded-current-turn current-val)))
          (assoc
            (if (:current-scored? g-state)
                (update current-val :total inc)
                current-val)
            :recorded-current-turn true)
          (if (and (:ready g-state)
                   (:recorded-current-turn current-val))
              (update current-val :recorded-current-turn not)
              current-val)))
    {:total 0 :recorded-current-turn false}
    (fn [a] (:total (:value a)))))

(defn scratches_ [id]
  (Analytic. (str "scratches/" id)
    (fn [current-val g-state]
      current-val ;TODO
      )
    {:total 0 :recorded-current-turn false}
    (fn [a] (:total (:value a)))))
