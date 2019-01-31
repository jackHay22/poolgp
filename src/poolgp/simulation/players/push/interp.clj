(ns poolgp.simulation.players.push.interp
  (:require [poolgp.simulation.players.push.instructions :as instrs]
            [clojush.interpreter :as clojush-interp]
            [clojush.pushstate :as clojush-push])
  (:import poolgp.simulation.structs.Vector)
  (:gen-class))

(defn load-push
  "prepare push code"
  [push-cons]
  (second push-cons))

(defn- make-clojush-vec
  [^Vector v]
  )

(defn eval-push
  "evaluate push code based on tablestate"
  [ts push max-iterations inputs]
  (let [cue (filter #(= (:id %) :cue) (:balls ts))
        cue-location (:center cue)
        ball-locations (map :center (:balls ts))
        pocket-locations (:pockets (:table ts))
        state-w-inputs (reduce #(clojush-push/push-item %2 :input %1)
                                (clojush-push/make-push-state)
                                ;TODO inputs
                                (list 2 5))
        ;evaluation-termination-state (clojush-interp/run-push push state-w-inputs)
        updated-velocity (Vector. (- (rand-int 15) 7) (- (rand-int 15) 7))] ;TODO
        ;(println "Push evaluated to: " evaluation-termination-state)
        ;TODO: improve efficiency here
        (println state-w-inputs)
        (update-in ts [:balls]
          #(map (fn [b] (if (= (:id b) :cue)
                            (assoc b :vector updated-velocity)
                            b
            )) %)
          )))
