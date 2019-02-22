(ns poolgp.simulation.players.push
  (:require [clojush.interpreter :as clojush-interp]
            [clojush.pushstate :as clojush-push]
            [poolgp.config :as config])
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

(defn- make-clojush-vec
  "turn a vector into a clojush vec"
  [^Vector v]
  [(:x v) (:y v)])

(defn- push-vec-floats
  "push list of items to vector float stack"
  [state items]
  (reduce #(clojush-push/push-item (make-clojush-vec %2) :vector_float %1)
          state items))

(defn- push-ball-group
  "push a set of balls based on filter to push state"
  [state filter-fn]
  (push-vec-floats
    state
    (map :center (filter filter-fn
            (:balls @TABLESTATE-CACHE)))))

(clojush-push/define-registered cue
  (fn [state]
    (clojush-push/push-item
      (make-clojush-vec
        (:center (first (filter #(= (:id %) :cue)
                              (:balls @TABLESTATE-CACHE)))))
      :vector_float state)))

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
      (:pockets (:table @TABLESTATE-CACHE)))))

(clojush-push/define-registered ball-diam
  (fn [state]
    (clojush-push/push-item
      (* 2 config/BALL-RADIUS-M)
      :float state)))

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
        int-stack (:integer push-final-state)]
        (vec-nil-guard
          (cond
            (not (empty? vec-float-stack))
                    (Vector. (first (first vec-float-stack))
                             (second (first vec-float-stack)))
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
