(ns poolgp.simulation.pool.physics
  (:require [poolgp.simulation.structs :as structs]
            [poolgp.config :as config])
  (:import poolgp.simulation.structs.Vector)
  (:gen-class))

;speed at which a ball is considered to be stopped
(def SPEED-TOLERANCE 0.1)

(def NOT-BALL-TYPE {:striped :solid :solid :striped})

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
        velx (/ (+ (* (:x b1-speed)
                      (- (:mass b1) (:mass b2)))
                   (* 2 (:mass b2) (:x b2-speed)))
                (+ (:mass b1) (:mass b2)))
        vely (/ (+ (* (:y b1-speed)
                      (- (:mass b1) (:mass b2)))
                   (* 2 (:mass b2) (:y b2-speed)))
                (+ (:mass b1) (:mass b2)))]

        (assoc b1 :vector (Vector. velx vely))))

(defn segment-surface-normals
  "Vector (pt), Vector (pt), facing  -> Vector
  calculate surface normal of line segment"
  [v1 v2 facing]
  (let [dx (- (:x v2) (:x v1))
        dy (- (:y v2) (:y v1))]
        (list
          (structs/normalize (Vector. (- dy) dx))
          (structs/normalize (Vector. dy (- dx))))))

(defn **2 [x] (* x x))

(defn ball-intersects-segment?
  "check if the ball intersects with the line segment
  CITE: https://math.stackexchange.com/questions/275529/check-if-line-intersects-with-circles-perimeter"
  [ball pts]
  (let [cx (:x (:center ball))
        cy (:y (:center ball))
        r (:r ball)
        ax (- (:x (first pts)) cx) ;makes equation easier
        ay (- (:y (first pts)) cy)
        bx (- (:x (second pts)) cx)
        by (- (:y (second pts)) cy)
        a (- (+ (**2 ax) (**2 ay)) (**2 r))
        b (* 2 (+ (* ax (- bx ax)) (* ay (- by ay))))
        c (+ (**2 (- bx ax)) (**2 (- by ay)))
        disc (- (**2 b) (* 4 a c))]
        (if (<= disc 0)
            false
            ;TODO Fix
            (let [sqrt-disc (Math/sqrt disc)
                  t1 (/ (+ (- b) sqrt-disc) (* 2 a))
                  t2 (/ (- (- b) sqrt-disc) (* 2 a))]
                  (or (and (> t1 0) (> 1 t1))
                          (and (> t2 0) (> 1 t2)))))))

(defn do-segment-collision
  "take ball and intersecting segment pts
  and recompute ball movement vector"
  [ball pts]
  (let [surface-normals (segment-surface-normals
                          (first pts) (second pts) nil)
        ;TODO: determine correct surface normal
        norm (first surface-normals)]
        (assoc ball :vector (structs/minus (:vector ball)
                              (structs/scale norm
                                (* 2 (structs/dot (:vector ball) norm)))))))

(defn check-wall-collisions
  "check if ball has collided with any walls
  and update velocity accordingly"
  [ball walls]
  (reduce (fn [b w]
        (reduce (fn [b pts]
          (if (ball-intersects-segment? b pts)
              (reduced
                (do-segment-collision b pts))
              b))
    b (partition 2 (:points w))))
  ball walls))

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
        (check-wall-collisions b (:walls (:table state)))
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
      :else state)
    state))

(defn do-turn-state
  "check criteria for changing turns"
  [state]
  (if (balls-stopped? (:balls state))
    (let [current (:current state)
          waiting (:waiting state)]
          ;TODO don't change if current pocketed balls on current turn
          (assoc state :current waiting :waiting current))
    state))

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
                          b-list)))
        s))
    state (:balls state)))

(defn update-ball-positions
  "update the positions of balls"
  [balls]
  (map (fn [b]
          (update-in
            (update-in b
              [:center] structs/plus (:vector b))
              [:vector] structs/scale config/SURFACE-FRICTION)) ;TODO
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
