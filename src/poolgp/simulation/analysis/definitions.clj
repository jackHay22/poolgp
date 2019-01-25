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
  "generic attribute updater for analysis
  when player is current and attribute is incremented
  once per turn -> updated value
  game attribute is a keyword that should give a boolean"
  [gs current-val id game-attribute aggregation-attribute]
  (if (and (player-is-current? gs id)
           (not (:recorded-current-turn current-val)))
      (assoc
        (if (game-attribute gs)
            (update current-val
                  aggregation-attribute inc)
            current-val)
        :recorded-current-turn true)
      (if (and (:ready gs)
               (:recorded-current-turn current-val))
          (update current-val :recorded-current-turn not)
          current-val)))

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
      (on-current-unrecorded
        g-state current-val id
        :current-scored? :total))
    {:total 0 :recorded-current-turn false}
    (fn [a] (:total (:value a)))))

(defn scratches_ [id]
  (Analytic. (str "scratches/" id)
    (fn [current-val g-state]
      (on-current-unrecorded
        g-state current-val id
        :scratched? :scratches))
    {:scratches 0 :recorded-current-turn false}
    (fn [a] (:scratches (:value a)))))

(defn advanced_balls_ [id]
  (Analytic. (str "advanced_balls/" id)
    (fn [current-val gs]
      ;TODO: detect if, per move,
      ; a player puts (its) balls closer to pockets
      current-val
      )
    {:previous-avg-dist 0 :count 0}
    (fn [a] (:count (:value a)))))
