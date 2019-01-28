(ns poolgp.simulation.analysis.game.rules
  (:require [poolgp.simulation.resources :as resources]
            [poolgp.log :as log]
            [poolgp.simulation.analysis.game.table.physics :as physics])
  (:gen-class))

;speed at which a ball is considered to be stopped
(def SPEED-TOLERANCE 0.02)

(defn- balls-stopped?
  "check if all balls have stopped moving"
  [balls]
  (reduce #(if (or (> (Math/abs (:x (:vector %2))) SPEED-TOLERANCE)
                   (> (Math/abs (:y (:vector %2))) SPEED-TOLERANCE))
               (reduced false) %1) true balls))

(defn- swap-current
  "take gamestate, swap current and waiting players"
  [gs]
  (let [current (:current gs)]
    (assoc gs :current (:waiting gs)
              :waiting current)))

(defn- do-move-reset
  "clear flags if new turn
  (player already changed if new player)"
  [gs]
  (assoc gs :current-scored? false
            :scratched false))

(defn- do-turn-state
  "update player turns if balls stopped"
  [gs]
  (if (and
        (balls-stopped? (:balls (:table-state gs)))
        (not (:ready? gs)))
      ;start new turn
      (do-move-reset
        (assoc
          (if (and (:current-scored? gs)
                   (not (:scratched? gs)))
              gs
              (swap-current gs))
           :ready? true))
      gs))

(defn- pocketed?
  "determine if ball is in a pocket"
  [b table]
  (reduce #(if (> (:r table) (physics/distance (:center b) %2))
               (reduced true) %1)
          false (:pockets table)))

(defn- move-pocketed
  "take gamestate, check for balls in
  pockets, move to pocketed list"
  [gamestate]
  (update-in gamestate [:table-state]
    (fn [ts]
      (reduce (fn [s b]
                (if (pocketed? b (:table ts))
                  (if (not (= (:id b) :cue))
                    ;regular ball pocketed
                    (update-in
                      (update-in s
                        [:pocketed] conj b)
                        [:balls] (fn [balls]
                                    (filter
                                        #(not (= (:id %) (:id b)))
                                        balls)))
                    ;move cue to break point
                    (update-in s [:balls]
                      (fn [balls] (map #(if (= (:id %) :cue)
                                          (assoc % :center resources/BREAK-PT) %)
                                        balls))))
                 s))
              ts (:balls ts)))))

(defn- check-pocketed
  "if any balls are in pockets, determine
  :current-scored? or scratched
  NOTE: does not move balls to pocketed list"
  [gs]
  (let [current-balltype (:ball-type (:current gs))]
    (reduce #(if (pocketed? %2 (:table (:table-state gs)))
                 (if (= (:type %2) :cue)
                     (assoc %1 :scratched? true)
                     (cond
                       (= current-balltype :unassigned)
                           (update-in
                                (assoc
                                  (assoc-in %1 [:current :ball-type] (:type %2))
                                  :current-scored? true)
                                [:current :score] inc)
                       (= (:type %2) current-balltype)
                           (update-in
                                 (assoc %1 :current-scored? true)
                                 [:current :score] inc)
                       :opposing-ball
                           (assoc %1 :scratched? true)))
                  ;else return state (not pocketed)
                  %1)
            gs (:balls (:table-state gs)))))

(defn rules-update
  "check rules and gamestate"
  [gamestate]
  (do-turn-state
    (move-pocketed
      (check-pocketed gamestate))))

(defn rules-log
  "log important updates to rules
  (if start of new move)"
  [gs]
  (if (:ready? gs)
    (log/write-info (str "Player" (:id (:current gs)) "to move"))))
