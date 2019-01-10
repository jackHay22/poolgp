(ns poolgp.peripherals.window.infopanel
  (:require [poolgp.config :as config])
  (:import javax.swing.JPanel)
  (:import java.awt.Dimension)
  (:import java.awt.Color)
  (:import javax.swing.BoxLayout)
  (:import javax.swing.JLabel)
  (:import javax.swing.JButton)
  (:import java.awt.event.ActionListener)
  (:gen-class))

(def pause (proxy [ActionListener] []
          (actionPerformed [event]
            (swap! config/PAUSED? not))))

(defn get-score-panel
  []

  )

(defn get-control-panel
  []
  (let [panel (JPanel.)
        pause-button (JButton. "Start")]
  (.addActionListener pause-button pause)
  (doto panel
    (.setLayout (BoxLayout. panel BoxLayout/PAGE_AXIS))
    (.add pause-button)
  )
  panel))

(defn get-info-panel
  []
  (let [panel (JPanel.)]
    (doto panel
      (.setPreferredSize
        (Dimension. config/POOL-WIDTH-PX config/INFO-HEIGHT-PX))
      (.setLayout (BoxLayout. panel BoxLayout/LINE_AXIS))
      (.add (get-control-panel))
      (.add (JLabel. "Info Panel"))
    )
  panel))
