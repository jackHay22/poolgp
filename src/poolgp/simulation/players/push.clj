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
          (not (empty? vec-float-stack)) (Vector. (first (first vec-float-stack))
                                                  (second (first vec-float-stack)))
          (not (empty? vec-int-stack)) (Vector. (first (first vec-int-stack))
                                                (second (first vec-int-stack)))
          (not (empty? int-stack))
          :no-output (Vector. 0 0))))

(defn eval-push
  "evaluate push code based on tablestate"
  [ts push inputs]
  ;TODO: use input list
  (let [state-w-inputs (reduce #(clojush-push/push-item %2 :input %1)
                                (clojush-push/make-push-state)
                                (concat
                                  (conj
                                    (map make-clojush-vec (map :center (:balls ts)))
                                    (make-clojush-vec
                                        (:center (first
                                            (filter #(= (:id %) :cue) (:balls ts))))))
                                  (map make-clojush-vec (:pockets (:table ts)))))]
        (update-in ts [:balls]
          #(map (fn [b] (if (= (:id b) :cue)
                            (assoc b :vector
                              (extract-cue-vel
                                (clojush-interp/run-push
                                      push state-w-inputs)))
                            b)) %))))
