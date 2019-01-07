(ns poolgp.simulation.utils
  (:require [poolgp.simulation.resources :as resources])
  (:import poolgp.simulation.structs.Vector)
  (:gen-class))

(import java.util.Date)
(import java.text.SimpleDateFormat)
(import java.awt.image.BufferedImage)
(import java.awt.geom.AffineTransform)
(import java.awt.RenderingHints)
(import java.awt.geom.AffineTransform)
(import java.awt.image.AffineTransformOp)
(import java.awt.geom.Point2D$Double)

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

(defn get-rotation-op
  "return affine transform on angle, anchor pt"
  [angle anchor-x anchor-y]
  (AffineTransformOp.
    (AffineTransform/getRotateInstance angle anchor-x anchor-y)
    AffineTransformOp/TYPE_BILINEAR))

(defn draw-image-rotate
  "rotate image using affine transform operation"
  [gr x y img op]
  (.drawImage gr (.filter op img nil) (int x) (int y) nil))

(defn get-pt-transform
  "get transformed pt using affinetransformop"
  [x y op]
  (let [dest (Point2D$Double. 0 0)
        pt2d (.getPoint2D op (Point2D$Double. (double x) (double y)) dest)]
    (Vector. (.getX dest) (.getY dest))))

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
              (if (and (not (nil? ball-list))
                       (not (empty? ball-list)))
                  (if (list? ball-list)
                      (map f ball-list)
                      (f ball-list))
                  ball-list))))
    gs (list :balls :pocketed)))

(defn strip-images
  "strip loaded images from gamestate and
  replace with resource path"
  [gs]
  (reduce #(assoc-in %1 %2 ((last %2) resources/TABLE-IMAGES))
          (doto-balls gs #(assoc % :img ((:id %) resources/BALL-IMAGES)))
          (list [:table :surface] [:table :raised] [:controller :cue])))

(defn load-images
  "load gs images from resource paths"
  [gs]
  (reduce #(update-in %1 %2 load-image)
            (doto-balls gs
                #(update-in % [:img]
                  (fn [img-p] (scale-image-width
                      (load-image img-p) (* (:r %) 2)))))
            (list [:table :surface] [:table :raised] [:controller :cue])))

(defn read-state
  "load entities state from save file, take list of config states to merge with"
  [path]
  (with-open [save-reader (clojure.java.io/reader path)]
      (load-images
        (read-string (clojure.string/join "\n" (line-seq save-reader))))))

(defn write-state
  "write gamestate to file"
  ([gs filename]
    (with-open [save-writer (clojure.java.io/writer filename)]
      (.write save-writer (pr-str (strip-images gs)))))
  ([gs] (write-state gs (str (get-timestamp) ".txt"))))
