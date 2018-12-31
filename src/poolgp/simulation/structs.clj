(ns poolgp.simulation.structs
  (:require [poolgp.simulation.demo.renderutils :as renderutils])
  (:gen-class))

(defprotocol StateInterface
  (init-state [s c])
  (update-state [s c]))

;render on object, context Graphics2D object
(defprotocol Renderable (render [o c g]))

(defrecord SystemState [init-handler update-handler render-handler]
  StateInterface
  (init-state [state context]
    (init-handler context))
  (update-state [state context]
    (update-handler context))
  Renderable
  (render [state context graphics]
    (render-handler context graphics)))

;Player
; {
;   :id :p1/:p2
;   :type :genetic/:interactive
;   :strategy (push code)
;   :fitness int
;   :score int
;   :ball-type :solid/:striped/:unassigned
; }

(defrecord Player [id type strategy fitness score ball-type])


(defprotocol VecOps
  (dot [v1 v2])
  (scale [v s])
  (plus [v1 v2])
  (minus [v1 v2])
  (len-sqrd [v])
  (normalize [v]))

;Vector
; {
;   :x int
;   :y int
; }

(defrecord Vector [x y]
  VecOps
  (dot [v1 v2]
    (+ (* (:x v1) (:x v2))
       (* (:y v1) (:y v2))))
  (normalize [v]
    (let [mag (Math/sqrt (+ (* (:x v) (:x v))
                            (* (:y v) (:y v))))]
              (Vector. (/ (:x v) mag) (/ (:y v) mag))))
  (scale [v s]
    (Vector. (* s (:x v)) (* s (:y v))))
  (plus [v1 v2]
    (Vector. (+ (:x v1) (:x v2)) (+ (:y v1) (:y v2))))
  (minus [v1 v2]
    (Vector. (- (:x v1) (:x v2)) (- (:y v1) (:y v2))))
  (len-sqrd [v] (+ (* (:x v) (:x v)) (* (:y v) (:y v)))))

;Ball
; {
;   :center Vector.
;   :r int (radius)
;   :vector Vector. (dx dy)
;   :mass int
;   :id int
;   :type :solid/:striped
;   :img "" -> image
; }

(defrecord Ball [^Vector center r ^Vector vector mass id type img]
  Renderable
  (render [b c g]
    (do
      (if c
        (renderutils/render-ball-shadow g
          (- (:x (:center b)) (:r b))
          (- (:y (:center b)) (:r b)) (:r b)))
      (.drawImage g (:img b)
        (int (- (:x (:center b)) (:r b)))
        (int (- (:y (:center b)) (:r b))) nil))))

;Wall
;(polygon)
; {
;   :points (list Vector)
; }

(defrecord Wall [points])

;Table
; {
;   :r int
;   :pockets (list Vector)
;   :walls (list Wall)
;   :surface "" -> img
;   :raised "" -> img
; }

(defrecord Table [r pockets walls surface raised])

;GameState
; {
;   :p1 (Player.)
;   :p2 (Player.)
;   :current :p1/:p2
;   :waiting :p1/:p2
;   :cue (Ball.)
;   :balls [(Ball.) (Ball.)]
;   :pocketed [(Ball.) (Ball.)]
;   :table Table.
; }

(defrecord GameState [^Player p1 ^Player p2 current waiting ^Ball cue balls pocketed ^Table table])
