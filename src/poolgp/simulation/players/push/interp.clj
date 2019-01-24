(ns poolgp.simulation.players.push.interp
  (:require [poolgp.simulation.players.push.instructions :as instrs])
  (:import poolgp.simulation.structs.Vector)
  (:gen-class))

; PushStacks
; {
;   :integer (list int)
;   :exec (list fn)
; }
(defrecord PushStacks [integer vector boolean exec])

(def INSTRS-NS "poolgp.simulation.players.push.instructions/")

(defn- resolve-loaded-name
  "resolve action to qualified function name"
  [function-name family-marker]
  (ns-resolve *ns*
    (symbol (str INSTRS-NS function-name family-marker))))

(defn load-push
  "load push code from a string"
  ;TODO: check if exists
  [push-str]
  (map #(if (or (boolean? %) (number? %)) %
            (resolve-loaded-name % "_"))
        (read-string push-str)))

(defn- mk-stacks
  "make stacks from initial push listing"
  [push]
  (reduce (fn [state instr]
            (update state
              (cond
                ;TODO: integer vs. number
                (map? instr) :vector
                (boolean? instr) :boolean
                (number? instr) :integer
                :else :exec)
              conj instr))
          (PushStacks. (list) (list) (list) (list)) push))

(defn- evaluate-push-state
  "evaluate push state steps (up to max), return stacks"
  [push-stacks max-iterations]
  (loop [stacks push-stacks current-step 0]
         (if (and (> max-iterations current-step)
                  (> (count (:exec stacks)) 0))
              (let [exec-fn (first (:exec stacks))
                    stack-update (update-in stacks [:exec] #(drop 1 %))]
                (recur (exec-fn stack-update) (inc current-step)))
             stacks)))

(defn eval-push
  "evaluate push code based on tablestate"
  [ts push max-iterations inputs]
  ;TODO: use inputs
  (let [cue (filter #(= (:id %) :cue) (:balls ts))
        cue-location (:center cue)
        ball-locations (map :center (:balls ts))
        pocket-locations (:pockets (:table ts))
        stack-state (mk-stacks (concat push ball-locations))
        ;TODO: add locations
        updated-stacks (evaluate-push-state stack-state max-iterations)
        updated-velocity (Vector. -10 0)] ;TODO
        ;TODO: improve efficiency here
        (update-in ts [:balls]
          #(map (fn [b] (if (= (:id b) :cue)
                            (assoc b :vector updated-velocity)
                            b
            )) %)
          )
        ))
