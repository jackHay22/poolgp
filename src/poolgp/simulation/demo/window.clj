(ns poolgp.simulation.demo.window
  (:require [poolgp.simulation.structs :as structs])
  (:gen-class))

(import java.awt.image.BufferedImage)
(import javax.swing.JPanel)
(import javax.swing.JFrame)
(import java.awt.Graphics2D)
(import java.awt.Graphics)
(import java.awt.Dimension)
(import java.awt.event.MouseListener)

(def SYSTEM-THREAD (atom nil))
(def SLEEP-TICKS-PER-SECOND 1000)

;keeps track of state
(def STATE (atom nil))

(defn graphical-panel
  "-extends JPanel, implements Runnable-"
  [sys-state-record width height target-delay]
  (let [base-image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        g (cast Graphics2D (.createGraphics base-image))]
     (proxy [JPanel Runnable] []
            (addNotify []
              (do (proxy-super addNotify)
                  (if (= @SYSTEM-THREAD nil)
                      (reset! SYSTEM-THREAD (.start (Thread. this))))))
            (paintComponent [^Graphics panel-graphics]
              (proxy-super paintComponent panel-graphics)
              (structs/render sys-state-record @STATE g)
              (.drawImage panel-graphics base-image 0 0 width height nil))
            (run [] (loop []
                      (let [render-start (System/nanoTime)]
                      (do (reset! STATE (structs/update-state sys-state-record @STATE))
                          (.repaint this)
                          (Thread/sleep target-delay)))
                    (recur))))))

(defn start-window
  "start JFrame and add JPanel extension as content"
  [sys-state-record state-path window-setup]
  ;initialize state and store
  (reset! STATE (structs/init-state sys-state-record state-path))
  (let [panel (graphical-panel sys-state-record
                  (:width window-setup) (:height window-setup)
                  (/ SLEEP-TICKS-PER-SECOND (:fps window-setup)))
        window (JFrame. (:title window-setup))]
        (doto panel
          (.setPreferredSize
            (Dimension. (:width window-setup) (:height window-setup)))
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
