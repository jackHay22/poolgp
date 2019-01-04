(ns poolgp.simulation.pool.physics
  (:require [poolgp.simulation.structs :as structs]
            [poolgp.config :as config])
  (:import poolgp.simulation.structs.Vector)
  (:gen-class))

;speed at which a ball is considered to be stopped
(def SPEED-TOLERANCE 0.1)

(def NOT-BALL-TYPE {:striped :solid :solid :striped})

(defn ** [x] (* x x))

(defn distance
  "Distance formula"
  ([P1 P2] (distance (:x P1) (:y P1)
                     (:x P2) (:y P2)))
  ([x1 y1 x2 y2]
    (Math/sqrt (+ (** (- x2 x1)) (** (- y2 y1))))))

(defn vector-from-angle
  "create dx/dy vector from force/angle"
  [a f]
  (Vector. (- (int (* f (Math/cos a))))
           (- (int (* f (Math/sin a))))))

(defn pts-angle-radians
  "get angle of line from pt1 to pt2"
  [v1 v2]
  (Math/atan2
      (- (:y v2) (:y v1))
      (- (:x v2) (:x v1))))

(defn do-normal-reflection
  "perform a normal reflection of vector d on normal n
  (n is normalized)"
  [d n]
  (structs/minus d
    (structs/scale
      (structs/scale n (structs/dot d n)) 2)))

(defn do-ball-collision
  "recalculate movement vectors on collision
  CITE: https://ericleong.me/research/circle-circle/#dynamic-circle-circle-collision"
  [b1 b2]
  (let [norm (structs/normalize (structs/minus (:center b2) (:center b1)))
        p (/ (* 2
                (- (structs/dot (:vector b1) norm)
                   (structs/dot (:vector b2) norm)))
             (+ (:mass b1) (:mass b2)))]
       (assoc b1 :vector
          (structs/minus (:vector b1)
              (structs/scale
                  (structs/scale norm (:mass b1)) p)))))

(defn segment-surface-normals
  "Vector (pt), Vector (pt), facing  -> Vector
  calculate surface normal of line segment"
  [v1 v2 facing]
  (let [dx (- (:x v2) (:x v1))
        dy (- (:y v2) (:y v1))]
        (list
          (structs/normalize (Vector. (- dy) dx))
          (structs/normalize (Vector. dy (- dx))))))

(defn pt-on-segment
  "check if a point is on a line segment"
  [a b pt]

  )

(defn ball-intersects-segment?
  "check if the ball intersects with the line segment
  CITE: https://stackoverflow.com/questions/1073336/circle-line-segment-collision-detection-algorithm
  (using vector projection)"
  [ball pts]
  (let [epsilon 2
        segment-vec (structs/minus (second pts) (first pts))
        circle-vec (structs/minus (:center ball) (first pts))
        proj-pt (structs/plus (first pts) (structs/proj segment-vec circle-vec))
        segment-diff (- (distance (first pts) (second pts))
                        (+ (distance proj-pt (first pts))
                           (distance proj-pt (second pts))))]
        (and
          ;confirm that projection is on line segment
          (> epsilon segment-diff)
          (> segment-diff (- epsilon))
          (> (:r ball) (distance proj-pt (:center ball))))))

(defn do-segment-collision
  "take ball and intersecting segment pts
  and recompute ball movement vector"
  [ball pts]
  ;TODO
  ball)
  ; (let [surface-normals (segment-surface-normals
  ;                         (first pts) (second pts) nil)
  ;       ;TODO: determine correct surface normal
  ;       norm (first surface-normals)]
  ;       (assoc ball :vector (structs/minus (:vector ball)
  ;                             (structs/scale norm
  ;                               (* 2 (structs/dot (:vector ball) norm)))))))

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
