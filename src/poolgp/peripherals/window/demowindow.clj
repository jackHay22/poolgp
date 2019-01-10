(ns poolgp.peripherals.window.demowindow
  (:require [poolgp.config :as config]
            [poolgp.peripherals.window.infopanel :as info]
            [poolgp.peripherals.interaction.interactionutils :as interaction]
            [poolgp.simulation.manager :as manager])
  (:import java.awt.image.BufferedImage)
  (:import javax.swing.JPanel)
  (:import javax.swing.JFrame)
  (:import java.awt.Graphics2D)
  (:import java.awt.Graphics)
  (:import java.awt.Dimension)
  (:import java.awt.event.MouseListener)
  (:import javax.swing.BoxLayout)
  (:gen-class))

(def SYSTEM-THREAD (atom nil))
(def SLEEP-TICKS-PER-SECOND 1000)

;keeps track of state
(def STATE (atom nil))

(defn graphical-panel
  "-extends JPanel, implements Runnable-"
  [width height target-delay]
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
            (mousePressed [e])
            (mouseReleased [e])
            (paintComponent [^Graphics panel-graphics]
              (proxy-super paintComponent panel-graphics)
              (manager/simulation-render @STATE g)
              (.drawImage panel-graphics base-image 0 0 width height nil))
            (run [] (loop []
                      (if (not @config/PAUSED?)
                        (do (reset! STATE (manager/simulation-update @STATE))
                            (.repaint this)
                            (Thread/sleep target-delay)))
                    (recur))))))

(defn start-window
  "start JFrame and add JPanel extension as content"
  [state-path]
  ;initialize state and store
  (reset! STATE (manager/simulation-init state-path))
  (let [panel (graphical-panel
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
