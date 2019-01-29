(ns clojush.problems.software.poolgp
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io])
  (:import java.net.Socket)
  )

(use-clojush)

(def DISTRIBUTE-CHANNEL (async/chan))
(def COMPLETED-CHANNEL (async/chan))

;sockets
(def HOST-POOL (atom (list)))
(def CURRENT-HOST-I (atom 0))

(defn- distribution-channel-worker
  "reads from channel, sends to host from pool"
  []
  (async/go-loop []
    (let [hosts @HOST-POOL
          current @CURRENT-HOST-I
          writer (io/writer (nth hosts current))]
          (.write writer (async/<! DISTRIBUTE-CHANNEL))
          (reset! CURRENT-HOST-I
            (mod (inc current) (count hosts))))))

(defn- incoming-channel-worker
  "waits for incoming (evaluated) individuals"
  []
  ;TODO: recompile
  )

(defn- completion-listener
  "socket server listening for completed individuals"
  []
  )

(defn- register-host
  "add a socket to the host pool"
  [ip port]
  (swap! HOST-POOL conj (Socket. ip))
  )

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
