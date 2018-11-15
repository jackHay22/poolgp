(ns poolgp.simulation.window
  (:require [poolgp.simulation.manager :as manager])
  (:gen-class))

(import java.awt.image.BufferedImage)
(import javax.swing.JPanel)
(import javax.swing.JFrame)
(import java.awt.Graphics2D)
(import java.awt.Graphics)
(import java.awt.Dimension)

(defn get-drawable-panel
  "get a jpanel that passes Graphics2D obj to manager on repaint"
  [w h]
  (let [base-image (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)
        g (cast Graphics2D (.createGraphics base-image))]
        (proxy [JPanel] []
          (paintComponent [^Graphics panel-graphics]
            (proxy-super paintComponent panel-graphics)
            (manager/render g)
            (.drawImage panel-graphics base-image 0 0 w h nil)))))

(defn start-window
  "start JFrame and add JPanel extension as content"
  [title width height]
  (let [panel (get-drawable-panel width height)
        window (JFrame. title)]
        (doto panel
          (.setPreferredSize (Dimension. width height))
          (.setFocusable true)
          (.requestFocus))
        (doto window
          (.setContentPane panel)
          (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
          (.setResizable false)
          (.pack)
          (.setVisible true)
          (.validate)
          (.repaint))
    ;return repaint handler
    #(.repaint panel)))

(defn init-demo
  "start demo window"
  [state]
  (manager/init (assoc state :repaint
      (start-window "Pool GP" 400 200))))
