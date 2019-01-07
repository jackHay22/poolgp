(ns poolgp.simulation.eval.testbuilder
  (:require [poolgp.simulation.utils :as utils]
            [poolgp.simulation.structs :as structs]
            [poolgp.simulation.resources :as resources]
            [poolgp.config :as config])
  (:import poolgp.simulation.structs.GameState)
  (:import poolgp.simulation.structs.Player)
  (:gen-class))

(import java.awt.image.BufferedImage)
(import javax.swing.JPanel)
(import javax.swing.JFrame)
(import java.awt.Graphics2D)
(import java.awt.Graphics)
(import java.awt.Dimension)
(import java.awt.event.MouseListener)
(import javax.swing.JMenu)
(import javax.swing.JMenuBar)
(import javax.swing.JMenuItem)
(import java.awt.event.ActionListener)

(def EDIT-STATE (atom nil))
(def GRAPHICS-PANEL (atom nil))

(defrecord EditState [filename selected-ball gs])

(defn set-new-gs!
  "reset gs atom with new blank state"
  [filename]
  (reset! EDIT-STATE
    (EditState.
      filename
      nil ;selected ball
      (utils/load-images
        (GameState.
          (Player. :p1 :genetic (list) 0 0 :unassigned)
          (Player. :p2 :genetic (list) 0 0 :unassigned)
          :p1
          :p2
          (list) ;balls
          (list) ;pocketed
          resources/TABLE
          resources/CONTROLLER)))))

(defn refresh! [] (.repaint @GRAPHICS-PANEL))

(defn display-selected-ball
  [gr b]
  (.drawRect 0 0 20 20)
  (utils/draw-image gr 0 0 (:img b)))

(defn render-builder-window
  "render window components"
  [gr edit-state]
  (let [gs (:gs edit-state)]
    (do
      (utils/draw-image gr 0 0 (:surface (:table gs)))
      (doall (map #(structs/render % true gr) (:balls gs)))
      (if (not (= nil (:selected-ball edit-state)))
        (display-selected-ball gr (:selected-ball edit-state)))
      (utils/draw-image gr 0 0 (:raised (:table gs))))))

(defn static-panel!
  "create a static, mouselistening panel"
  [width height]
  (let [base-image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        g (cast Graphics2D (.createGraphics base-image))]
        (proxy [JPanel MouseListener] []
               (mouseClicked [e])
               (mouseEntered [e])
               (mouseExited [e])
               (mousePressed [e])
               (mouseReleased [e])
               (paintComponent [^Graphics panel-graphics]
                 (proxy-super paintComponent panel-graphics)
                 (render-builder-window g @EDIT-STATE)
                 (.drawImage panel-graphics base-image 0 0 width height nil)))))

(def add-new-cue!
     (proxy [ActionListener] []
             (actionPerformed [event]
                ;check if cue already added
               )))

(def add-new-ball!
      (proxy [ActionListener] []
              (actionPerformed [event]

                )))

(def deselect!
      (proxy [ActionListener] []
              (actionPerformed [event]

                )))

(def save!
      (proxy [ActionListener] []
              (actionPerformed [event]
                (let [edit-state @EDIT-STATE]
                (utils/write-state
                  (:gs edit-state) (:filename edit-state))))))

(defn get-toolbar
  []
  (let [bar (JMenuBar.)
        file (JMenu. "File")
        edit (JMenu. "Edit")
        save (JMenuItem. "Save")
        add-cue (JMenuItem. "Add Cue")
        add-ball (JMenuItem. "Add Ball")
        deselect (JMenuItem. "Deselect")]
        (do
          (.add file save)
          (.addActionListener save save!)
          (.add edit add-cue)
          (.addActionListener add-cue add-new-cue!)
          (.add edit add-ball)
          (.addActionListener add-ball add-new-ball!)
          (.add edit deselect)
          (.addActionListener deselect deselect!)
          (doto bar
            (.add file)
            (.add edit)))))

(defn make-test
  "open graphical window for creating a new
  test state"
  [filename]
  (let [edit-window (JFrame. "PoolGP Test Builder")
        table-panel (static-panel! config/POOL-WIDTH-PX config/POOL-HEIGHT-PX)]
      (do (set-new-gs! filename)
          (reset! GRAPHICS-PANEL table-panel)
          (doto table-panel
            (.setPreferredSize
              (Dimension. config/POOL-WIDTH-PX (+ config/POOL-HEIGHT-PX 50)))
            (.setFocusable true)
            (.setLayout nil)
            (.addMouseListener table-panel)
            (.requestFocus))
          (doto edit-window
            (.add table-panel)
            (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
            (.setResizable false)
            (.pack)
            (.setJMenuBar (get-toolbar))
            (.setVisible true)
            (.validate)
            (.repaint)))))
