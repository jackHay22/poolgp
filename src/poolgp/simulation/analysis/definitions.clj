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
                   (:total current))))))

(def-analytic scratches
  (fn [gs current p-id]
    ;TODO: produces incorrect value (switches player before scratched?)
    (if (and (player-is-current? gs p-id)
             (:scratched? gs))
      (inc current) current)))

(def-analytic turns
  (fn [gs current p-id]
    (if (player-is-current? gs p-id)
        (inc current) current)))
