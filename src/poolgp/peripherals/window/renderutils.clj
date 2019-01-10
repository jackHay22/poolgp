(ns poolgp.peripherals.window.renderutils
  (:require [poolgp.config :as config])
  (:import java.awt.geom.Point2D)
  (:import java.awt.Color)
  (:import java.awt.RadialGradientPaint)
  (:import java.awt.Graphics2D)
  (:import java.awt.AlphaComposite)
  (:import java.awt.image.BufferedImage)
  (:import java.awt.MultipleGradientPaint)
  (:gen-class))

(defn render-ball-shadow
  "render a shadow below a pool ball"
  [gr x y r]
  (let [width (* r 2.2)
        full-bound (* r 8)
        perspective-bounds (java.awt.geom.Rectangle2D$Float. (float x) (float y) (float width) (float width))
        shadow-layer (BufferedImage. config/WINDOW-WIDTH-PX config/WINDOW-HEIGHT-PX BufferedImage/TYPE_INT_ARGB)
        g2d (cast Graphics2D (.createGraphics shadow-layer))
        dist (float-array [0.1 1.0])
        radial-color (into-array Color [(Color. 0 0 0 100) (Color. 0.0 0.0 0.0 0.0)])
        gradient (RadialGradientPaint. perspective-bounds dist radial-color
                        java.awt.MultipleGradientPaint$CycleMethod/NO_CYCLE)]
    (do
      (.setPaint g2d gradient)
      (.setComposite g2d (AlphaComposite/getInstance AlphaComposite/SRC_OVER 0.95))
      (.fillRect g2d 0 0 config/WINDOW-WIDTH-PX config/WINDOW-HEIGHT-PX)
      (.drawImage gr shadow-layer 0 0 config/WINDOW-WIDTH-PX config/WINDOW-HEIGHT-PX nil)
      (.dispose g2d))))
