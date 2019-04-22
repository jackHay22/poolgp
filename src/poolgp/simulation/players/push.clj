(ns poolgp.simulation.players.push
  (:require [clojush.interpreter :as clojush-interp]
            [clojush.pushstate :as clojush-push]
            [poolgp.config :as config]
            [poolgp.simulation.resources :as resources]
            [poolgp.simulation.players.vectors :as vec])
  (:import poolgp.simulation.structs.Vector)
  (:gen-class))

(use '(clojush.instructions
             boolean code common numbers random-instructions
             environment string char vectors
             tag zip input-output genome gtm))

;holds the gamestate for a push run so that input
;instructions work
(def TABLESTATE-CACHE (atom nil))
(def CURRENT-BALLTYPE (atom nil))

(def VEC-TYPE :vector_float)

(def BALL-DIAM (* 2 config/BALL-RADIUS-PX))

(defn- make-clojush-vec
  "turn a vector into a clojush vec"
  [^Vector v]
  [(:x v) (:y v)])

(defn- push-vec-floats
  "push list of items to vector float stack"
  [state items]
  (reduce #(clojush-push/push-item
              (make-clojush-vec %2) VEC-TYPE %1)
          state items))

(defn- push-ball-group
  "push a set of balls based on filter to push state"
  [state filter-fn]
  (push-vec-floats
    state
    (map :center (filter filter-fn
            (:balls @TABLESTATE-CACHE)))))

(defn- pop-multiple
  "pop muliple items from given state stack"
  [state types]
  (reduce #(clojush-push/pop-item %2 %1)
          state types))

(defn- gen-direction-to-wall-reflector
  "take a target pocket for reflecting shot and
  select wall to use for reflection
  [vec] -> [[x1 y1] [x2 y2]]"
  ;TODO: only uses top and bottom
  [vec-dir vec2]
  ((if (and (>= (count vec-dir) 2)
            (>= (count vec2) 2))
      (let [x-right? (> (first vec-dir) 0)
            y-down? (> (second vec-dir) 0)]
            (cond
              (and x-right? y-down?) :top
              x-right? :bottom
              y-down? :top
              :default :bottom))
      ;default
      :bottom) resources/MAIN-WALL-SEGMENT-REFLECTORS))

; ---------------------------PoolGP input instructions----------------------------

(clojush-push/define-registered cue
  (fn [state]
    (clojush-push/push-item
      (make-clojush-vec
        (:center (first (filter #(= (:id %) :cue)
                              (:balls @TABLESTATE-CACHE)))))
      VEC-TYPE state)))

(clojush-push/define-registered self-balls
  (fn [state]
    (let [current-balltype @CURRENT-BALLTYPE]
      (push-ball-group state
        (if (= current-balltype :unassigned)
                        #(not (= (:type %) :cue))
                        #(= (:type %) current-balltype))))))

(clojush-push/define-registered opp-balls
  (fn [state]
    (let [current-balltype @CURRENT-BALLTYPE]
      (push-ball-group state
        (if (= current-balltype :unassigned)
                        #(not (= (:type %) :cue))
                        #(not (= (:type %) current-balltype)))))))

(clojush-push/define-registered pockets
  (fn [state]
    (push-vec-floats state
      resources/POCKET-OPENINGS)))

(clojush-push/define-registered ball-diam
  (fn [state]
    (clojush-push/push-item
      BALL-DIAM :float state)))

(clojush-push/define-registered self-count
  (fn [state]
    (clojush-push/push-item
      (count
        (filter
          (if (= @CURRENT-BALLTYPE :unassigned)
              #(not (= (:type %) :cue))
              #(= (:type %) @CURRENT-BALLTYPE))
          (:balls @TABLESTATE-CACHE)))
      :integer state)))

(clojush-push/define-registered opp-count
  (fn [state]
    (clojush-push/push-item
      (count
        (filter
          (if (= @CURRENT-BALLTYPE :unassigned)
              #(not (= (:type %) :cue))
              #(not (= (:type %) @CURRENT-BALLTYPE)))
          (:balls @TABLESTATE-CACHE)))
      :integer state)))

; ---------------------------PoolGP complex instructions----------------------------

(clojush-push/define-registered pt_of_wall_reflection
  ;takes two points (ex. cue and pocket), determines
  ;target wall for reflecting shot, and gives back pt on wall
  (fn [state]
    (if (>= (count (VEC-TYPE state)) 2)
        (let [vec1 (first (VEC-TYPE state))
              vec2 (second (VEC-TYPE state))
              reflector (gen-direction-to-wall-reflector
                              (vec/sub vec2 vec1) vec2)
              v1-reflect (reflector vec1)
              off-wall-traj (vec/sub vec2 v1-reflect)]
              ;TODO; incomplete
            (clojush-push/push-item off-wall-traj
               VEC-TYPE
               (pop-multiple state
                 (repeat 2 VEC-TYPE))))
        state)))

(clojush-push/define-registered ball_diam_normal
  ;takes vector and normalizes it to diameter of ball
  (fn [state]
    (if (not (empty? (VEC-TYPE state)))
        (clojush-push/push-item
          (vec/normal
            (first (VEC-TYPE state)) BALL-DIAM)
           VEC-TYPE
           (clojush-push/pop-item VEC-TYPE state))
        state)))

(clojush-push/define-registered pt_glancing
  ;takes two vectors (ex. ball, pocket)
  ;and calculates position required for cue ball to
  ;contect first ball on trajectory to hit third vec
  (fn [state]
    (if (>= (count (VEC-TYPE state)) 2)
        (let [vec1 (first (VEC-TYPE state))
              vec2 (second (VEC-TYPE state))
              norm-diff (vec/normal (vec/sub vec2 vec1) BALL-DIAM)]
            (clojush-push/push-item (vec/sub vec1 norm-diff)
               VEC-TYPE
               (pop-multiple state
                 (repeat 2 VEC-TYPE))))
        state)))

(clojush-push/define-registered will_collide?
  ;takes pt and trajectory and determines if this course
  ;will collide with a third pt (ball diam implied)
  (fn [state]
    (if (>= (count (VEC-TYPE state)) 3)
      (let [vec1 (first (VEC-TYPE state))
            vec2 (second (VEC-TYPE state))
            vec3 (nth (VEC-TYPE state) 2)
            intersect-pt (vec/add
                            vec1
                            (vec/proj vec2
                              (vec/sub vec3 vec1)))]
          (clojush-push/push-item
            (>= BALL-DIAM
                (vec/len
                  (vec/sub vec3 intersect-pt)))
             :boolean
             (pop-multiple state
               (repeat 3 VEC-TYPE))))
      state)))

; ----------------------------------------------------------------------------------

(defn- vec-nil-guard
  "prevent velocity vector from containing a nil value"
  [vector s]
  (if (or (nil? (:x vector)) (nil? (:y vector)))
    (Vector. 0 0)
     vector))

(defn- extract-cue-vel
  "take final push state and return a Vector for cue dx dy"
  [push-final-state]
  (let [vec-int-stack (:vector_integer push-final-state)
        vec-float-stack (:vector_float push-final-state)
        int-stack (:integer push-final-state)
        float-stack (:float push-final-state)]
        (vec-nil-guard
          (cond
            (not (empty? vec-float-stack))
                    (Vector. (first (first vec-float-stack))
                             (second (first vec-float-stack)))
            (>= (count float-stack) 2)
                    (Vector. (first float-stack)
                             (second float-stack))
            (>= (count int-stack) 2)
                   (Vector. (first int-stack)
                            (second int-stack))
            (> (count int-stack) 0)
                    (Vector. 0 (first int-stack))
            :no-output (Vector. 0 0))
          push-final-state)))

(defn eval-push
  "evaluate push code based on tablestate"
  [ts push inputs p-balltype]
  ;update gs cache
  (do
    (reset! TABLESTATE-CACHE ts)
    (reset! CURRENT-BALLTYPE p-balltype)
    (update-in ts [:balls]
      ;add new velocity to cue
      #(map (fn [b] (if (= (:id b) :cue)
                        (assoc b :vector
                          (extract-cue-vel
                            (clojush-interp/run-push push
                              (clojush-push/make-push-state))))
                        b)) %))))
