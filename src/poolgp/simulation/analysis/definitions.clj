(ns poolgp.simulation.analysis.definitions
  (:require [poolgp.log :as log]
            [poolgp.simulation.analysis.game.table.physics :as physics])
  (:gen-class))

(defmacro def-analytic
  "macro for defining an analytic state transformation
  Takes operation. Operation is then applied to gamestate
  and current value in record"
  [name op]
  (list 'def (symbol name)
        (list 'fn '[gamestate analytics-record p-id]
            (list 'assoc 'analytics-record
              (keyword name)
              (list op 'gamestate
                       (list (keyword name) 'analytics-record)
                       'p-id)))))

(defn- player-is-current?
  "check if player is current player"
  [gs id]
  (= id (:id (:current gs))))

(defn- get-player
  "get player from gamestate by id"
  [gs id]
  (if (player-is-current? gs id)
      (:current gs) (:waiting gs)))

(defn- distance-to-closest-pocket
  "returns ball distance to closet pocket"
  [pt pockets]
  (reduce #(max %1 (physics/distance pt %2)) 0 pockets))

;; ------ ANALYTICS DEFINITIONS ------

(def-analytic score
  (fn [gs current p-id]
    (let [p (get-player gs p-id)
          p-balltype (:ball-type p)]
      (assoc current
        :score (:score p)
        :total (if (and (nil? (:total current))
                        (not (= :unassigned p-balltype)))
                   (+ (:score p) ;include points already scored
                      (count (filter #(= (:type %) p-balltype)
                              (:balls (:table-state gs)))))
                   nil)))))

(def-analytic forward-movement
  (fn [gs current p-id]
    ;TODO will this work correctly? NO
    (if (player-is-current? gs p-id)
      (let [current-avg (:avg current)
            prev-dist (:prev current)
            player-balltype (:ball-type (get-player gs p-id))
            target-balls (filter
                          (if (= :unassigned player-balltype)
                              #(not (= (:type %) :cue))
                              #(= (:type %) player-balltype))
                          (:balls (:table-state gs)))
            current-agg-dist (reduce #(+ (distance-to-closest-pocket
                                            (:center %2)
                                            (:pockets (:table (:table-state gs))))
                                          %1)
                                     0 target-balls)]
          (assoc current
            :avg (/ (+ current-avg (- current-agg-dist prev-dist)) 2)
            :prev current-agg-dist))
        current)))

(def-analytic scratches
  (fn [gs current p-id]
    ;TODO: produces incorrect value
    (if (and (player-is-current? gs p-id)
             (:scratched? gs))
      (inc current) current)))

(def-analytic scored-turns
  (fn [gs current p-id]
    (if (and (player-is-current? gs p-id)
             (:current_scored? gs))
        (assoc (update current :count inc)
          :best (max (inc (:count current))
                     (:best current)))
        (assoc current :count 0))))
