(ns poolgp.simulation.analysis.game.rules
  (:require [poolgp.simulation.resources :as resources]
            [poolgp.log :as log]
            [poolgp.config :as config]
            [poolgp.simulation.structs :as structs]
            [poolgp.simulation.analysis.game.table.physics :as physics])
  (:import poolgp.simulation.structs.Vector)
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

(defn- on-table?
  "determine if a ball is in bounds
  (may be the case for clipping bug or
  during placement)
  (essentially checks if ball in window)"
  [b]
  (let [bx (:x b) by (:y b)]
    (and
      (> 0 bx) (> 0 by)
      (> config/POOL-WIDTH-PX bx)
      (> config/POOL-HEIGHT-PX by))))

(defn- check-game-complete
  "check if the game is completed"
  [gs]
  (assoc gs :game-complete?
    (reduce (fn [c b-type]
              (if (empty?
                    (filter #(= (:type %) b-type)
                              (:balls (:table-state gs))))
                  (reduced true) c))
            false (list :solid :striped))))

(defn- replace-ball
  "replace ball on table (checks for collisions
   with other table balls)
   --positions ball at first non colliding (moves left
     from attempted position)
   location"
  [ball attempt balls]
  (reduce
    (fn [b pos]
      (if (reduce #(if (or (physics/ball-collision? b %2)
                           (not (on-table? b)))
                       (reduced false) %1) true balls)
          ;return ball, zero vel (position already worked)
          (reduced (assoc b :vector (Vector. 0 0)))
          ;try next position
          (assoc b :center pos)))
    (assoc ball :center attempt)
    (iterate #(structs/minus
                % (Vector. config/BALL-RADIUS-PX 0))
             attempt)))

(defn- fix-off-table
  "move any balls that have left the table
  to a location on the table"
  [gamestate]
  (update-in gamestate [:table-state :balls]
    (fn [balls]
      (map #(if (not (on-table? %))
                (replace-ball %
                  resources/BREAK-PT balls)
                %))
           balls)))

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
                                          (replace-ball %
                                              resources/BREAK-PT balls) %)
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
                                  (assoc-in
                                    (assoc-in %1 [:current :ball-type] (:type %2))
                                                 [:waiting :ball-type] (if (= (:type %2) :solid)
                                                                            :striped :solid))
                                  :current-scored? true)
                                [:current :score] inc)
                       (= current-balltype (:type %2))
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
  (check-game-complete
    (do-turn-state
      (fix-off-table
        (move-pocketed
          (check-pocketed gamestate))))))

(defn rules-log
  "log important updates to rules
  (if start of new move)"
  [gs]
  (if (:ready? gs)
    (log/write-info (str "Player" (:id (:current gs)) "to move"))))
