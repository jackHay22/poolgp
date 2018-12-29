(ns poolgp.simulation.pool.physics
  (:require [poolgp.simulation.structs :as structs]
            [poolgp.config :as config])
  (:import poolgp.simulation.structs.Vector)
  (:gen-class))

;speed at which a ball is considered to be stopped
(def SPEED-TOLERANCE 0.5)

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
  [balls table]
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
              b (:walls table))
      balls))
    balls))

(defn update-balls
  "update balls on table"
  [balls table]
  (map (fn [b]
          (update-in
            (update-in b
              [:center] #(structs/plus % (:vector b)))
              [:vector] #(structs/scale % config/SURFACE-FRICTION))) ;TODO
       (do-collisions balls table)))

(defn balls-stopped?
  "check if all balls have stopped moving"
  [balls]
  (reduce #(if (> (:x (:vector %2)) SPEED-TOLERANCE)
               (reduced false) %1) true balls))

(defn pocketed?
  "determine if ball is in a pocket"
  [b table]
  (reduce #(if (> (:r %2) (distance b %2))
               (reduced true) %1) false (:pockets table)))
