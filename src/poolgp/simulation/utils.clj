(ns poolgp.simulation.utils
  (:require [poolgp.simulation.resources :as resources])
  (:gen-class))

(import java.util.Date)
(import java.text.SimpleDateFormat)
(import java.awt.image.BufferedImage)
(import java.awt.geom.AffineTransform)
(import java.awt.RenderingHints)

(def write-log (fn [msg] (println "poolgp =>" msg)))

(def path? (fn [path] (.isFile (clojure.java.io/file path))))

(def get-timestamp
  (fn [] (.format (SimpleDateFormat. "yyyy-MM-dd_HH.mm.ss") (Date.))))

(defn draw-image
  "draw image"
  [gr x y img]
  (try
    (.drawImage gr img x y nil)
    (catch Exception e
      (write-log (str "Failed to render image:" img "\n" (.getMessage e))))))

(defn load-image
  "load an image from resources"
  [path]
  (javax.imageio.ImageIO/read (clojure.java.io/resource path)))

(defn scale-image
  "scale buffered image"
  [buffered-image s]
  (let [scaled (BufferedImage. (int (* s (.getWidth buffered-image)))
                               (int (* s (.getHeight buffered-image)))
                               BufferedImage/TYPE_INT_ARGB)
        g2d (.createGraphics scaled)
        transform (AffineTransform/getScaleInstance s s)]
      (do
        (.setRenderingHint g2d RenderingHints/KEY_INTERPOLATION
                           RenderingHints/VALUE_INTERPOLATION_BICUBIC)
        (.drawImage g2d buffered-image transform nil)
        (.dispose g2d)
        scaled)))

(defn scale-image-width
  "scale an image by new width"
  [image new-width]
  (scale-image image (/ new-width (.getWidth image))))

(defn doto-balls
  "perform update fn on each ball"
  [gs f]
  (reduce (fn [state current]
        (update-in state [current]
            (fn [ball-list]
              (map f ball-list))))
    gs (list :balls :pocketed)))

(defn strip-images
  "strip loaded images from gamestate and
  replace with resource path"
  [gs]
  (reduce #(assoc-in %1 [:table %2] (%2 resources/TABLE-IMAGES))
          (doto-balls gs #(assoc % :img ((:id %) resources/BALL-IMAGES)))
          (list :surface :raised)))

(defn load-images
  "load gs images from resource paths"
  [gs]
  (reduce #(update-in %1 [:table %2] load-image)
            (doto-balls gs
                #(update-in % [:img]
                  (fn [img-p] (scale-image-width
                      (load-image img-p) (* (:r %) 2)))))
            (list :surface :raised)))

(defn read-state
  "load entities state from save file, take list of config states to merge with"
  [path]
  (with-open [save-reader (clojure.java.io/reader path)]
      (load-images
        (read-string (clojure.string/join "\n" (line-seq save-reader))))))

(defn write-state
  "write gamestate to file"
  [gs]
  (with-open [save-writer (clojure.java.io/writer (str (get-timestamp) ".txt"))]
    (.write save-writer (pr-str (strip-images gs)))))
