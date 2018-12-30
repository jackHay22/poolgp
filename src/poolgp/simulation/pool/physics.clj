(ns poolgp.simulation.pool.physics
  (:require [poolgp.simulation.structs :as structs]
            [poolgp.config :as config])
  (:import poolgp.simulation.structs.Vector)
  (:gen-class))

;speed at which a ball is considered to be stopped
(def SPEED-TOLERANCE 0.5)

(def NOT-BALL-TYPE {:striped :solid :solid :striped})
(def NOT-PLAYER-TYPE {:p1 :p2 :p2 :p1})

(defn distance
  "Distance formula"
  ([P1 P2] (distance (:x P1) (:y P1)
                     (:x P2) (:y P2)))
  ([x1 y1 x2 y2]
    (let [xdif (- x2 x1) ydif (- y2 y1)]
      (Math/sqrt (+ (* xdif xdif) (* ydif ydif))))))

(defn do-ball-collision
  "recalculate movement vectors on collision
  https://gamedevelopment.tutsplus.com/tutorials/when-worlds-collide-simulating-circle-circle-collisions--gamedev-769"
  [b1 b2]
  (let [b1-speed (:vector b1)
        b2-speed (:vector b2)
        ; cx (/ (+ (* (:x b1) (:r b2))
        ;          (* (:x b2) (:r b1)))
        ;       (+ (:r b1) (:r b2)))
        ; cy (/ (+ (* (:y b1) (:r b2))
        ;          (* (:y b2) (:r b1)))
        ;       (+ (:r b1) (:r b2)))
        velx (/ (+ (* (:x b1-speed)
                      (- (:mass b1) (:mass b2)))
                   (* 2 (:mass b2) (:x b2-speed)))
                (+ (:mass b1) (:mass b2)))
        vely (/ (+ (* (:y b1-speed)
                      (- (:mass b1) (:mass b2)))
                   (* 2 (:mass b2) (:y b2-speed)))
                (+ (:mass b1) (:mass b2)))]

        (assoc b1 :vector (Vector. velx vely))))

(defn do-wall-collision
  "recalculate movement vectors on collision with wall"
  [b wall]
  )

(defn wall-collision?
  "https://bitlush.com/blog/circle-vs-polygon-collision-detection-in-c-sharp"
  [b wall]
  (let [radius-sqrd (* (:r b) (:r b))]

  ))

(defn ball-collision?
  "determine if collision between balls"
  [b1 b2]
  (> (+ (:r b1) (:r b2))
    (distance
      (:center b1) (:center b2))))

(defn do-collisions
  "determine if ball has collided with a wall
  update ball accordingly"
  [state]
  (update-in state [:balls]
    (fn [balls]
      (map (fn [b]
        ;check ball collisions
        (reduce (fn [current other]
                  (if (and (not (= (:id current) (:id other)))
                           (ball-collision? current other))
                      (do-ball-collision current other)
                      current))
        ;check wall collisions
        (reduce (fn [ball wall]
                      (if (wall-collision? ball wall)
                          (reduced
                            (do-wall-collision ball wall))
                          ball))
                  b (:walls (:table state)))
          balls))
        balls))))

(defn balls-stopped?
  "check if all balls have stopped moving"
  [balls]
  (reduce #(if (> (:x (:vector %2)) SPEED-TOLERANCE)
               (reduced false) %1) true balls))

(defn pocketed?
  "determine if ball is in a pocket"
  [b table]
  (reduce #(if (> (:r table) (distance (:center b) %2))
               (reduced true) %1)
          false (:pockets table)))

(defn do-game-state
  "check game status and ball type assignments"
  [state]
  (if (not (empty? (:pocketed state)))
    (cond
      (= (:ball-type ((:current state) state)) :unassigned)
        ;do ball type assignments (based on first ball in pocketed list)
        (let [pocketed-type (:type (first (:pocketed state)))]
          (assoc-in
            (assoc-in state
              [(:current state) :ball-type] pocketed-type)
              [(:waiting state) :ball-type] (pocketed-type NOT-BALL-TYPE)))
      :else state
  )
  state)
  )

(defn do-turn-state
  "check criteria for changing turns"
  [state]
  (if (balls-stopped? (:balls state))
    (let [current (:current state)
          waiting (:waiting state)]
          ;TODO don't change if current pocketed balls on current turn
          (assoc state :current waiting :waiting current)
          ) state))

(defn do-pockets
  "if ball pocketed, remove from table"
  [state]
  (reduce (fn [s b]
    (if (pocketed? b (:table s))
        (update-in
          (update-in s
            [:pocketed] conj b)
            [:balls] (fn [b-list]
                          (filter #(not (= (:id b) (:id %)))
                          b-list))) s))
    state (:balls state)))

(defn update-ball-positions
  "update the positions of balls"
  [balls]
  (map (fn [b]
          (update-in
            (update-in b
              [:center] #(structs/plus % (:vector b)))
              [:vector] #(structs/scale % config/SURFACE-FRICTION))) ;TODO
       balls))

(defn update-state
  "update balls on table"
  [state]
  (update-in
    (do-turn-state
      (do-game-state
        (do-pockets
          (do-collisions state)))) [:balls]
    update-ball-positions))
