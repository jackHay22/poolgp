(ns poolgp.simulation.players.push
  (:require [clojush.interpreter :as clojush-interp]
            [clojush.pushstate :as clojush-push])
  (:import poolgp.simulation.structs.Vector)
  (:gen-class))

(use '(clojush.instructions
             boolean code common numbers random-instructions
             environment string char vectors
             tag zip input-output genome gtm))

(defn load-push
  "prepare push code"
  [push-cons]
  (second push-cons))

(defn- make-clojush-vec
  "turn a vector into a clojush vec"
  [^Vector v]
  [(int (:x v)) (int (:y v))])

(defn- make-poolgp-vec
  "turn a vec into a Vector."
  [v]
  (if (> (count v) 1)
      (Vector. (first v) (second v))
      (Vector. 0 0)))

(defn eval-push
  "evaluate push code based on tablestate"
  [ts push max-iterations inputs]
  ;TODO: use input list
  (let [cue-input (make-clojush-vec
                      (:center (first (filter #(= (:id %) :cue) (:balls ts)))))
        ball-inputs (map make-clojush-vec (map :center (:balls ts)))
        pocket-inputs (map make-clojush-vec (:pockets (:table ts)))
        state-w-inputs (reduce #(clojush-push/push-item %2 :input
                                    (clojush-push/push-item %2 :vector_integer %1))
                                (clojush-push/make-push-state)
                                (concat
                                  (conj ball-inputs cue-input)
                                  pocket-inputs))]
        (update-in ts [:balls]
          #(map (fn [b] (if (= (:id b) :cue)
                            (assoc b :vector
                              (make-poolgp-vec (first
                                (:vector_integer (clojush-interp/run-push
                                                    push state-w-inputs)))))
                            b)) %))))
