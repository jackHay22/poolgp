(ns clojush.problems.software.poolgp)

(use-clojush)

(defn- distribute-to-eval
  "send individual to server"
  [indiv]
  )

(def argmap
  {:error-function #(distribute-to-eval %)
   :atom-generators (list (fn [] (lrand-int 10))
                          'integer_+
                          'integer_-
                          'integer_**
                          'integer_*
                          'integer_dup
                          'integer_sqrt
                          'integer_gt
                          'integer_lt
                          'integer_max
                          'integer_min
                          'integer_eq
                          'integer_mod
                          'exec_dup
                          'exec_if
                          'exec_dotimes
                          'boolean_and
                          'boolean_or
                          'boolean_not
                          'vector_dot
                          'vector_norm
                          'vector_scale
                          'vector_plus
                          'vector_minus
                          'vector_proj
                          'vector_len
                          'vector_dup
                          'vector_new
                          'vector_int)
   :parent-selection :tournament
   :genetic-operator-probabilities {:alternation 0.5
                                    :uniform-mutation 0.5}
   })
