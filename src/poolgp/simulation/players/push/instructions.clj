(ns poolgp.simulation.players.push.instructions
  (:require [poolgp.simulation.structs :as structs])
  (:import poolgp.simulation.structs.Vector)
  (:gen-class))

(defn- pop-if-exists
  "take arg list + state and param vec
  if a val exists on target stack, move to list
  in state and remove from original state"
  [state stack-name]
  (let [target-stack (stack-name (second state))]
    (if (not (empty? target-stack))
        (assoc-in
          (update-in state [0] conj (first target-stack))
          [1 stack-name] (drop 1 target-stack))
        state)))

(defn- instr-operation
  "perform push operation defined be instruction"
  [push-state in-stack-vec out-stack operation]
  (let [extract-args (reduce pop-if-exists (list [] push-state) in-stack-vec)]
        (if (= (count (first extract-args)) (count in-stack-vec))
            (let [output (apply operation (first extract-args))]
                (update (second extract-args) out-stack
                  (if (list? output)
                      #(concat output %)
                      #(conj % output))))
            ;no-op
            push-state)))

(defmacro definstr
  "macro for defining a push instruction
  takes: name, stack to pop args from , arity of operation,
  stack to push args onto, and operation to perform on args"
  [name in-stack-vec out-stack op]
  (list 'def (symbol (str name "_"))
        (list 'fn '[push-state]
            (list 'instr-operation 'push-state
                   in-stack-vec out-stack op))))


;___________________ Instruction Definitions ____________________

(definstr integer_+ [:integer :integer] :integer +)
(definstr integer_+ [:integer :integer] :integer -)
(definstr integer_** [:integer] :integer #(* % %))
(definstr integer_* [:integer :integer] :integer *)
(definstr integer_dup [:integer] :integer #(list % %))

(definstr exec_dup [:exec] :exec #(list % %))

(definstr vector_dot [:vector :vector] :integer structs/dot)
(definstr vector_norm [:vector] :vector structs/normalize)
(definstr vector_scale [:vector :integer] :vector structs/scale)
(definstr vector_plus [:vector :vector] :vector structs/plus)
(definstr vector_minus [:vector :vector] :vector structs/minus)
(definstr vectro_proj [:vector :vector] :vector structs/proj)
(definstr vector_len [:vector] :integer structs/len)
(definstr vector_dup [:vector] :vector #(list % %))
(definstr vector_new [:integer :integer] :vector #(Vector. %1 %2))
(definstr vector_int [:vector] :integer #(list (:x %) (:y %)))
