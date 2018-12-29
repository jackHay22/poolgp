(ns poolgp.simulation.demo.infopanel
  (:require [poolgp.config :as config])
  (:gen-class))

(import javax.swing.JPanel)
(import java.awt.Dimension)
(import java.awt.Color)
(import javax.swing.BoxLayout)
(import javax.swing.JLabel)

(defn get-score-panel
  []
  
  )

(defn get-info-panel
  []
  (let [panel (JPanel.)]
  (doto panel
    (.setPreferredSize
      (Dimension. config/POOL-WIDTH-PX config/INFO-HEIGHT-PX))
    (.setLayout (BoxLayout. panel BoxLayout/LINE_AXIS))
    (.add (JLabel. "Info Panel"))
  )
  panel))
