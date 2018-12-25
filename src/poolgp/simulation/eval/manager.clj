(ns poolgp.simulation.eval.manager
  (:gen-class))

(defn eval-indiv
  "evaluate an individual"
  [indiv]
  (println indiv)
  (println "Sleeping for 10 seconds...")
  (Thread/sleep 10000)
  (println "Reporting fitness...")
  (assoc indiv :test true)
)

(defn eval-init [config])
(defn eval-update [state])
(defn eval-render [state _])
