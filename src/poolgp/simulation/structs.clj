(ns poolgp.simulation.structs
  (:import java.awt.image.AffineTransformOp)
  (:gen-class))

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
;   :table-state (TableState.)
;   :p1 (Player.)
;   :p2 (Player.)
;   :current :p1/:p2
;   :waiting :p1/:p2
;   :changing-turn? true/false
;   :controller (ControllerInterface.)
; }

(defrecord GameState [^TableState table-state
                       ^Player p1 ^Player p2
                       current waiting changing-turn?
                       ^ControllerInterface controller])

;Protocol for evaluating a pool-test
(defprotocol Scorable (evaluate [test]))

;PoolTest
; {
;   :game-state (GameState.)
;   :max-decisions int
;   :eval-fn
;   :player-target :p1/:p2
; }

;TODO
(defrecord PoolTest [^GameState game-state max-decisions eval-fn player-target]
    Scorable
    (evaluate [test]
      ((:eval-fn test) (:gamestate test))))

(defprotocol Analyzable (analyze [analytic state]))

; TurnAnalytic
; {
;   :name :identifier
;   :value something
;   :operation (fn [current-val gs]) -> new-value
; }

(defrecord TurnAnalytic
  ;record that defines a datapoint to record
  ;when evaluating an individual (updated once per turn)
  [name value operation]
  Analyzable
  (analyze [analytic gamestate]
    (assoc analytic :value
      ((:operation analytic)
        (:value analytic) gamestate))))

;AnalysisState
; {
;   :game-state GameState.
;   :p1-analytics (list (TurnAnalytic.))
;   :p2-analytics (list (TurnAnalytic.))
; }

(defrecord AnalysisState [^GameState game-state p1-analytics p2-analytics])

;SimulationState
; {
;   :analytics-states (list (AnalyticsState.))
;   :max-iterations int
;   :current-iteration int
;   :connection socket/port etc...
;   :endgame-fn (fn [analytics-states]) -> fitness
; }

(defrecord SimulationState [analysis-states max-iterations
                            current-iteration connection])
