(ns poolgp.simulation.structs
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
;   :type :genetic/:interactive
;   :strategy (push code)
;   :fitness int
; }

(defrecord Player [type strategy fitness])


(defprotocol VecOps
  (dot [v1 v2])
  (scale [v s])
  (plus [v1 v2])
  (minus [v1 v2])
  (len-sqrd [v]))

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
  (scale [v s]
    (Vector. (* s (:x v)) (* s (:y v))))
  (plus [v1 v2]
    (Vector. (+ (:x v1) (:x v2)) (+ (:y v1) (:y v2))))
  (minus [v1 v2]
    (Vector. (- (:x v1) (:x v2)) (- (:y v1) (:y v2))))
  (len-sqrd [v] (+ (* (:x v) (:x v)) (* (:y v) (:y v)))))

;Ball
; {
;   :center Point.
;   :r int (radius)
;   :vector Vector. (dx dy)
;   :mass int
;   :id int
;   :img "" -> image
; }

(defrecord Ball [^Vector center r ^Vector vector mass id type img]
  Renderable
  (render [b c g]
    (.drawImage g (:img b)
      (int (- (:x (:center b)) (:r b)))
      (int (- (:y (:center b)) (:r b))) nil)))

;Wall
;(polygon)
; {
;   :points (list Vector)
; }

(defrecord Wall [points]
  Renderable
  (render [w c g]

    ))

;Table
; {
;   :r int
;   :pockets (list Vector)
;   :walls (list Wall)
; }

(defrecord Table [r pockets walls bg]
  Renderable
  (render [t c g]
    (.drawImage g (:bg t) 0 0 nil)
    (map render walls)))

;GameState
; {
;   :p1 (Player.)
;   :p2 (Player.)
;   :cue (Ball.)
;   :current :p1/:p2
;   :balls [(Ball.) (Ball.)]
;   :pocketed [(Ball.) (Ball.)]
;   :table Table.
; }

(defrecord GameState [p1 p2 current cue balls pocketed ^Table table])
