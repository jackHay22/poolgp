(ns poolgp.simulation.structs
  (:require [poolgp.simulation.demo.renderutils :as renderutils])
  (:import java.awt.geom.Ellipse2D$Float)
  (:import java.awt.image.AffineTransformOp)
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
  (len [v])
  (proj [v1 v2])
  (normalize [v]))

;Vector
; {
;   :x int
;   :y int
; }

;square fn
(defn ** [x] (* x x))

(defrecord Vector [x y]
  VecOps
  (dot [v1 v2]
    (+ (* (:x v1) (:x v2))
       (* (:y v1) (:y v2))))
  (normalize [v]
    (let [mag (Math/sqrt (+ (** (:x v))
                            (** (:y v))))]
              (Vector. (/ (:x v) mag) (/ (:y v) mag))))
  (scale [v s]
    (Vector. (* s (:x v)) (* s (:y v))))
  (plus [v1 v2]
    (Vector. (+ (:x v1) (:x v2)) (+ (:y v1) (:y v2))))
  (minus [v1 v2]
    (Vector. (- (:x v1) (:x v2)) (- (:y v1) (:y v2))))
  (proj [v1 v2] (scale v1 (/ (dot v1 v2) (** (len v1)))))
  (len-sqrd [v] (+ (** (:x v)) (** (:y v))))
  (len [v] (Math/sqrt (+ (** (:x v))
                         (** (:y v))))))

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
        (int (- (:y (:center b)) (:r b))) nil)
      ; (.draw g (Ellipse2D$Float. (- (:x (:center b)) (:r b))
      ;                            (- (:y (:center b)) (:r b))
      ;                            (* 2 (:r b)) (* 2 (:r b))))
                                 )))

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

;ControllerInterface
; {
;   :mouse-entered? true/false
;   :mouse (Vector.)
;   :force int
;   :angle int (radians)
;   :release? true/false
;   :cue path -> img
;   :rotate-op (AffineTransformOp.)/nil
;   :cue-draw (Vector.)
; }

(defrecord ControllerInterface [mouse-entered? ^Vector mouse
                                force angle release? cue
                                ^AffineTransformOp rotate-op
                                ^Vector cue-draw])

;GameState
; {
;   :p1 (Player.)
;   :p2 (Player.)
;   :current :p1/:p2
;   :waiting :p1/:p2
;   :balls [(Ball.) (Ball.)]
;   :pocketed [(Ball.) (Ball.)]
;   :table Table.
;   :controller (ControllerInterface.)
; }

(defrecord GameState [^Player p1 ^Player p2 current waiting balls pocketed
                      ^Table table ^ControllerInterface controller])

;Protocol for evaluating a pool-test
(defprotocol Scorable (evaluate [test]))

;PoolTest
; {
;   :gamestate (GameState.)
;   :max-decisions int
;   :eval-fn
;   :player-target :p1/:p2
; }

(defrecord PoolTest [^GameState gamestate max-decisions eval-fn player-target]
    Scorable
    (evaluate [test]
      ((:eval-fn test) (:gamestate test))))
