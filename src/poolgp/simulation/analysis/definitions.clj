(ns poolgp.simulation.analysis.definitions
  (:require [poolgp.log :as log])
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
    (:score (get-player gs p-id))))

(def-analytic forward-movement
  ;If player has scored, update current score
  (fn [gs current p-id]
    current
    ;TODO
    ))

(def-analytic scratches
  (fn [gs current p-id]
    (if (and (player-is-current? gs p-id)
             (:scratched? gs))
      (inc current) current)))

(def-analytic scored-turns
  (fn [gs current p-id]
    (if (and (player-is-current? gs p-id)
             (:scratched? gs))
      (inc current) current)))
