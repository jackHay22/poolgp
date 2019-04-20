(ns poolgp.simulation.structs
  (:import java.awt.image.AffineTransformOp)
  (:gen-class))

;Player
; {
;   :id :p1/:p2
;   :clojush-indiv (individual.)
;   :type :genetic/:interactive
;   :strategy (push code)
; }

(defrecord Player [id clojush-indiv type strategy])

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
;   :x int/float
;   :y int/float
; }

;square fn
(defn- ** [x] (* x x))

(defn- safe-div
  "safe division"
  [a b]
  (if (= 0 b)
    a (/ a b)))

(defrecord Vector [x y]
  VecOps
  (dot [v1 v2]
    (+ (* (:x v1) (:x v2))
       (* (:y v1) (:y v2))))
  (normalize [v]
    (let [mag (Math/sqrt (+ (** (:x v))
                            (** (:y v))))]
              (Vector. (safe-div (:x v) mag) (safe-div (:y v) mag))))
  (scale [v s]
    (Vector. (* s (:x v)) (* s (:y v))))
  (plus [v1 v2]
    (Vector. (+ (:x v1) (:x v2)) (+ (:y v1) (:y v2))))
  (minus [v1 v2]
    (Vector. (- (:x v1) (:x v2)) (- (:y v1) (:y v2))))
  (proj [v1 v2] (scale v1 (safe-div (dot v1 v2) (** (len v1)))))
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

(defrecord Ball [^Vector center r ^Vector vector mass id type img])

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

;TableState
;{
;   :balls [(Ball.) (Ball.)]
;   :pocketed [(Ball.) (Ball.)]
;   :table Table.
;}

(defrecord TableState [balls pocketed table])

;ControllerInterface
; {
;   :mouse-entered? true/false
;   :release? true/false
;   :cue path -> img
; }

(defrecord ControllerInterface [mouse-entered? release? cue])
;GamePlayer
; {
;   :id :p1/:p2
;   :ball-type :striped/:solid/:unassigned
;   :score int
; }

(defrecord GamePlayer [id ball-type score])

;GameState
; {
;   :table-state (TableState.)
;   :current GamePlayer
;   :waiting GamePlayer
;   :ready? true/false -> balls stopped, time for player move
;   :current-scored? true/false -> current player scored on turn
;   :push-inputs (list :cue :balls :pockets) (by default)
; }

(defrecord GameState [^TableState table-state
                       ^GamePlayer current
                       ^GamePlayer waiting
                       ready? current-scored?
                       scratched?
                       game-complete?
                       push-inputs])

;PlayerAnalytics
; {
;   :score {:score (int) :total (int)}
;   :scratches (int) total scratches
;   :scored-turns (int)
; }

(defrecord PlayerAnalytics [score balls-remaining])

;AnalysisState
; {
;   :game-state GameState.
;   :p1-analytics PlayerAnalytics.
;   :p2-analytics PlayerAnalytics.
; }

(defrecord AnalysisState [^GameState game-state p1-analytics p2-analytics])

;SimulationState
; {
;   :analytics-states (list (AnalyticsState.))
;   :max-iterations int
;   :current-iteration int
;   :p1 Player.
;   :p2 Player.
;   :demo true/false
;   :controller (ControllerInterface.)
; }

(defrecord SimulationState [analysis-states max-iterations
                            current-iteration
                            watching
                            ^Player p1 ^Player p2
                            ^ControllerInterface controller])

; ServerConfig
; {
;   :indiv-ingress-p int
;   :indiv-egress-p int
;   :opp-pool-req-p int
;   :engine-hostname str
; }
(defrecord ServerConfig [indiv-ingress-p indiv-egress-p
                         opp-pool-req-p engine-hostname])
