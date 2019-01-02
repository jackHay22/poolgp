(ns poolgp.simulation.demo.window
  (:require [poolgp.simulation.structs :as structs]
            [poolgp.config :as config]
            [poolgp.simulation.demo.infopanel :as info]
            [poolgp.simulation.demo.interactionutils :as interaction])
  (:gen-class))

(import java.awt.image.BufferedImage)
(import javax.swing.JPanel)
(import javax.swing.JFrame)
(import java.awt.Graphics2D)
(import java.awt.Graphics)
(import java.awt.Dimension)
(import java.awt.event.MouseListener)
(import javax.swing.BoxLayout)

(def SYSTEM-THREAD (atom nil))
(def SLEEP-TICKS-PER-SECOND 1000)

;keeps track of state
(def STATE (atom nil))

(defn graphical-panel
  "-extends JPanel, implements Runnable-"
  [sys-state-record width height target-delay]
  (let [base-image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        g (cast Graphics2D (.createGraphics base-image))]
     (proxy [JPanel Runnable MouseListener] []
            (addNotify []
              (do (proxy-super addNotify)
                  (if (= @SYSTEM-THREAD nil)
                      (reset! SYSTEM-THREAD (.start (Thread. this))))))
            (mouseClicked [e] (reset! STATE (interaction/clicked e @STATE)))
            (mouseEntered [e] (reset! STATE (interaction/entered e @STATE)))
            (mouseExited [e] (reset! STATE (interaction/exited e @STATE)))
            (mousePressed [e] ) ;TODO
            (mouseReleased [e]) ;TODO
            (paintComponent [^Graphics panel-graphics]
              (proxy-super paintComponent panel-graphics)
              (structs/render sys-state-record @STATE g)
              (.drawImage panel-graphics base-image 0 0 width height nil))
            (run [] (loop []
                      (let [render-start (System/nanoTime)]
                        (if (not @config/PAUSED?)
                          (do (reset! STATE (structs/update-state sys-state-record @STATE))
                              (.repaint this)
                              (Thread/sleep target-delay))))
                    (recur))))))

(defn start-window
  "start JFrame and add JPanel extension as content"
  [sys-state-record state-path]
  ;initialize state and store
  (reset! STATE (structs/init-state sys-state-record state-path))
  (let [panel (graphical-panel sys-state-record
                  config/POOL-WIDTH-PX config/POOL-HEIGHT-PX
                  (/ SLEEP-TICKS-PER-SECOND config/WINDOW-FPS))
        window (JFrame. config/WINDOW-TITLE)]
        (doto panel
          (.setPreferredSize
            (Dimension. config/POOL-WIDTH-PX config/POOL-HEIGHT-PX))
          (.setFocusable true)
          (.addMouseListener panel)
          (.requestFocus))
        (doto window
          (.setLayout (BoxLayout. (.getContentPane window) BoxLayout/PAGE_AXIS))
          (.add panel)
          (.add (info/get-info-panel))
          (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
          (.setResizable false)
          (.pack)
          (.setVisible true)
          (.validate)
          (.repaint))))
