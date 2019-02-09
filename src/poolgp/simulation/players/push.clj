(ns poolgp.simulation.players.push
  (:require [clojush.interpreter :as clojush-interp]
            [clojush.pushstate :as clojush-push])
  (:import poolgp.simulation.structs.Vector)
  (:gen-class))

(use '(clojush.instructions
             boolean code common numbers random-instructions
             environment string char vectors
             tag zip input-output genome gtm))

(defn- make-clojush-vec
  "turn a vector into a clojush vec"
  [^Vector v]
  [(:x v) (:y v)])

(defn- extract-cue-vel
  "take final push state and return a Vector for cue dx dy"
  [push-final-state]
  (let [vec-int-stack (:vector_integer push-final-state)
        vec-float-stack (:vector_float push-final-state)
        int-stack (:integer push-final-state)]
        (cond
          (not (empty? vec-float-stack))
                  (Vector. (first (first vec-float-stack))
                           (second (first vec-float-stack)))
          (not (empty? vec-int-stack))
                  (Vector. (first (first vec-int-stack))
                           (second (first vec-int-stack)))
          (>= (count int-stack) 2)
                  (Vector. (first int-stack)
                           (second int-stack))
          :no-output (Vector. 0 0))))

(defn- get-push-state
  "add table state inputs to a new push state"
  [ts inputs]
  ;TODO: use input list
  ; Creates input: [pocket locations, ball locations, cue location]
  (reduce (fn [s in] (clojush-push/push-item in :input s))
      (clojush-push/make-push-state)
      (concat
        (conj
          ;get ball positions
          (map make-clojush-vec (map :center (:balls ts)))
          ;get cue position
          (make-clojush-vec
              (:center (first
                  (filter (fn [b] (= (:id b) :cue))
                          (:balls ts))))))
        ;get pocket positions
        (map make-clojush-vec (:pockets (:table ts))))))

(defn eval-push
  "evaluate push code based on tablestate"
  [ts push inputs]
  (update-in ts [:balls]
    ;add new velocity to cue
    #(map (fn [b] (if (= (:id b) :cue)
                      (assoc b :vector
                        (extract-cue-vel
                          (clojush-interp/run-push push
                            (get-push-state ts inputs))))
                      b)) %)))
