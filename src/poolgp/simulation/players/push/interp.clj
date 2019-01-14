(ns poolgp.simulation.players.push.interp
  (:require [poolgp.simulation.players.push.instructions :as instrs])
  (:gen-class))

; PushStacks
; {
;   :integer (list int)
;   :exec (list fn)
; }
(defrecord PushStacks [integer vector exec])

(def INSTRS-NS "poolgp.simulation.players.push.instructions/")

(defn- resolve-loaded-name
  "resolve action to qualified function name"
  [function-name family-marker]
  (ns-resolve *ns*
    (symbol (str INSTRS-NS function-name family-marker))))

(defn load-push
  "load push code from a string"
  [push-str]
  (map #(resolve-loaded-name % "_") (read-string push-str)))

(defn- mk-stacks
  "make stacks from initial push listing"
  [push]
  (reduce (fn [state instr]
            (update state
              (cond
                ;TODO: integer vs. number
                (map? instr) :vector
                (number? instr) :integer
                (fn? instr) :exec)
              conj instr))
          (PushStacks. (list) (list) (list)) push))

(defn eval-push
  "evaluate push code based on tablestate"
  [ts push]
  (let [cue-location (:center (filter #(= (:id %) :cue) (:balls ts)))
        ball-locations (map :center (:balls ts))
        stack-state (mk-stacks push)]
        ;TODO: operate on tablestate with computed values
        ; note: integer stack elems will become cue force (dx/dy?)
  ts))
