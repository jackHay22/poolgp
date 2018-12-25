(ns poolgp.simulation.utils
  (:gen-class))

(import java.util.Date)
(import java.text.SimpleDateFormat)

(def write-log (fn [msg] (println "poolgp =>" msg)))

(def path? (fn [path] (.isFile (clojure.java.io/file path))))

(def get-timestamp
  (fn [] (.format (SimpleDateFormat. "yyyy-MM-dd_HH.mm.ss") (Date.))))

(defn draw-image
  [gr x y img]
  (try
    (.drawImage gr img x y nil)
    (catch Exception e
      (write-log (str "Failed to render image:" img "\n" (.getMessage e))))))

(defn load-image
  "load an image from resources"
  [path]
  (javax.imageio.ImageIO/read (clojure.java.io/resource path)))

(defn read-state
  "load entities state from save file, take list of config states to merge with"
  [path]
  (with-open [save-reader (clojure.java.io/reader path)]
    (read-string (clojure.string/join "\n" (line-seq save-reader)))))

(defn write-state
  "GameState -> path"
  [gs]
  (with-open [save-writer (clojure.java.io/writer (str (get-timestamp) ".txt"))]
    (.write save-writer (pr-str gs))))
