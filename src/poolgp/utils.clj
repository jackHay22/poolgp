(ns poolgp.utils
  (:gen-class))
  
(defn draw-image
  [gr x y img]
  (try
    (.drawImage gr img x y nil)
    (catch Exception e
      (println "poolgp => Failed to render image:" img "\n" (.getMessage e)))))

(defn load-image
  "load an image from resources"
  [path]
  (javax.imageio.ImageIO/read (clojure.java.io/resource path)))
