(ns poolgp.simulation.demo.window
  (:require [poolgp.simulation.demo.manager :as manager])
  (:gen-class))

(import java.awt.image.BufferedImage)
(import javax.swing.JPanel)
(import javax.swing.JFrame)
(import java.awt.Graphics2D)
(import java.awt.Graphics)
(import java.awt.Dimension)

(def SYSTEM-THREAD (atom nil))
(def SLEEP-TICKS-PER-SECOND 1000)

;keeps track of state
(def STATE (atom nil))

(defn graphical-panel
  "-extends JPanel, implements Runnable and KeyListener-"
  [width height target-delay]
  (let [base-image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        g (cast Graphics2D (.createGraphics base-image))]
     (proxy [JPanel Runnable] []
            (addNotify []
              (do (proxy-super addNotify)
                  (if (= @SYSTEM-THREAD nil)
                      (reset! SYSTEM-THREAD (.start (Thread. this))))))
            (paintComponent [^Graphics panel-graphics]
              (proxy-super paintComponent panel-graphics)
              (manager/demo-render @STATE g)
              (.drawImage panel-graphics base-image 0 0 width height nil))
            (run [] (loop []
                      (let [render-start (System/nanoTime)]
                      (do (reset! STATE (manager/demo-update @STATE))
                          (.repaint this)
                          (Thread/sleep target-delay)))
                    (recur))))))

(defn start-window
  "start JFrame and add JPanel extension as content"
  [state title width height framerate]
  (let [panel (graphical-panel width height
                  (/ SLEEP-TICKS-PER-SECOND framerate))
        window (JFrame. title)]
        (reset! STATE state)
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
          (.repaint))))
