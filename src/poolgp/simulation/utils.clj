(ns poolgp.simulation.utils
  (:require [clojure.data.json :as json])
  (:import java.util.Date)
  (:import java.text.SimpleDateFormat)
  (:import java.awt.image.BufferedImage)
  (:import java.awt.geom.AffineTransform)
  (:import java.awt.RenderingHints)
  (:import java.awt.geom.AffineTransform)
  (:import java.awt.image.AffineTransformOp)
  (:import java.awt.geom.Point2D$Double)
  (:gen-class))

(def write-log (fn [msg] (println "poolgp =>" msg)))

(def path? (fn [path] (.isFile (clojure.java.io/file path))))

(def get-timestamp
  (fn [] (.format (SimpleDateFormat. "yyyy-MM-dd_HH.mm.ss") (Date.))))

(defn draw-image
  "draw image"
  [gr x y img]
  (try
    (.drawImage gr img (int x) (int y) nil)
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

(defn load-image
  "load an image from resources"
  [path]
  (javax.imageio.ImageIO/read (clojure.java.io/resource path)))

(defn load-structure-images
  "load an image in a structure
  based on path vector(s)"
  [structure & path-vecs]
  (reduce #(update-in %1 %2 load-image) structure path-vecs))

(defn updates-in
  "expansion of update-in"
  [structure & vec-fns]
  (reduce #(update-in %1 (first %2) (second %2))
          structure (partition 2 vec-fns)))

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

(defn read-json-file
  "read json file into clojure map"
  [path]
  (with-open [save-reader (clojure.java.io/reader path)]
      (json/read-str
        (clojure.string/join "\n" (line-seq save-reader))
        :key-fn keyword)))

(defn write-json-file
  [path structure]
  (with-open [save-writer (clojure.java.io/writer path)]
    (.write save-writer (json/write-str structure))))

(defn get-edited-filename
  [filename]
  "test!.txt"
  )
