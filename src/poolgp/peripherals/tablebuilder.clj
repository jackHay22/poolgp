(ns poolgp.peripherals.tablebuilder
  (:require [poolgp.simulation.utils :as utils]
            [poolgp.simulation.structs :as structs]
            [poolgp.simulation.resources :as resources]
            [poolgp.config :as config]
            [poolgp.simulation.analysis.game.table.physics :as physics])
  (:import poolgp.simulation.structs.GameState)
  (:import poolgp.simulation.structs.Player)
  (:import poolgp.simulation.structs.Vector)
  (:import poolgp.simulation.structs.Ball)
  (:import java.awt.image.BufferedImage)
  (:import java.awt.Graphics2D)
  (:import java.awt.Graphics)
  (:import java.awt.Dimension)
  (:import java.awt.event.MouseListener)
  (:import java.awt.event.ActionListener)
  (:import javax.swing.JMenu)
  (:import javax.swing.JMenuBar)
  (:import javax.swing.JMenuItem)
  (:import javax.swing.JPanel)
  (:import javax.swing.JFrame)
  (:gen-class))

(def EDIT-STATE (atom nil))
(def GRAPHICS-PANEL (atom nil))

(def EMPTY-TABLE {:game {:table {:balls []}}})

(defrecord EditState [filename selected-ball
                      selected-holder surface
                      raised cue striped solid
                      table-json-struct])

(defn- set-new-es!
  "reset gs atom with new blank state"
  [filename]
  (reset! EDIT-STATE
    (EditState.
      filename
      nil ;selected ball
      (utils/load-image (:holder resources/TABLE-IMAGES))
      (utils/load-image (:surface resources/TABLE-IMAGES))
      (utils/load-image (:raised resources/TABLE-IMAGES))
      (utils/load-image (:cue resources/BALL-IMAGES))
      (utils/load-image (:striped resources/BALL-IMAGES))
      (utils/load-image (:solid resources/BALL-IMAGES))
      EMPTY-TABLE)))

(defn- on-write!
  "read current structure, add new structure, write back to file"
  [edit-state]
  (let [existing-json-struct (utils/read-json-file (:filename edit-state))]
    (if (map? existing-json-struct)
      (utils/write-json-file (:filename edit-state)
        (update-in existing-json-struct [:simulation :analysis]
                    conj (:table-json-struct edit-state))))))

(defn- refresh! [] (.repaint @GRAPHICS-PANEL))

(defn- display-ball
  "display a given ball (json structure)"
  [gr b-json edit-state]
  (utils/draw-image gr
            (- (:x b-json) config/BALL-RADIUS-PX)
            (- (:y b-json) config/BALL-RADIUS-PX)
            ((:type b-json) edit-state)))

(defn- display-selected-ball
  "display new ball in selected holder"
  [gr edit-state]
  (utils/draw-image gr 0 0 (:selected-holder edit-state))
  (display-ball gr (:selected-ball edit-state) edit-state))

(defn- render-builder-window
  "render window components"
  [gr edit-state]
  (let [balls (:balls (:table (:game (:table-json-struct edit-state))))]
    (do
      (utils/draw-image gr 0 0 (:surface edit-state))
      (doall (map #(display-ball gr % edit-state) balls))
      (utils/draw-image gr 0 0 (:raised edit-state))
      (if (not (= nil (:selected-ball edit-state)))
        (display-selected-ball gr edit-state)))))

(defn- add-ball-check-collisions!
  "adds ball to state (true) or false"
  [e]
  (let [state @EDIT-STATE
        ball-selected? (not (nil? (:selected-ball state)))]
    (if ball-selected?
        (let [current-balls (:balls (:table (:game (:table-json-struct state))))
              temp-position {:r config/BALL-RADIUS-PX
                             :center (Vector. (.getX e) (.getY e))}
              can-place? (reduce #(if (physics/ball-collision-static?
                                        temp-position {:r config/BALL-RADIUS-PX
                                                       :center (Vector. (:x %2) (:y %2))})
                                      (reduced false) %1)
                                  true current-balls)]
              (if can-place?
                (reset! EDIT-STATE
                    (assoc
                        (update-in state [:table-json-struct :game :table :balls]
                           conj (assoc (:selected-ball state)
                                 :x (.getX e)  :y (.getY e)))
                        :selected-ball nil)))
              can-place?)
            false)))

(defn- static-panel!
  "create a static, mouselistening panel"
  [width height]
  (let [base-image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        g (cast Graphics2D (.createGraphics base-image))]
        (proxy [JPanel MouseListener] []
               (mouseClicked [e]
                 (if (add-ball-check-collisions! e) (refresh!)))
               (mouseEntered [e])
               (mouseExited [e])
               (mousePressed [e])
               (mouseReleased [e])
               (paintComponent [^Graphics panel-graphics]
                 (proxy-super paintComponent panel-graphics)
                 (render-builder-window g @EDIT-STATE)
                 (.drawImage panel-graphics base-image 0 0 width height nil)))))

(defn- select-ball!
  "add a new ball to the selected-ball field
  of the edit state and refresh graphics"
  [id type]
  (do
    (reset! EDIT-STATE
        (assoc @EDIT-STATE :selected-ball
          {:x 40 :y 40 :id id :type type}))
      (refresh!)))

(def add-new-cue!
     (proxy [ActionListener] []
             (actionPerformed [event]
                ;check if cue already added
                (let [balls (:balls (:table (:game (:table-json-struct @EDIT-STATE))))
                      cue-added (filter #(= (:id %) :cue) balls)]
                  (if (empty? cue-added)
                      (select-ball! :cue :cue))))))

(defn- add-new-ball! [type]
      (proxy [ActionListener] []
              (actionPerformed [event]
                (select-ball! :1 type))))

(def deselect!
      (proxy [ActionListener] []
              (actionPerformed [event]
                (do
                  (reset! EDIT-STATE
                    (assoc @EDIT-STATE :selected-ball nil))
                    (refresh!)))))

(def write-current!
    (proxy [ActionListener] []
            (actionPerformed [event] (on-write! @EDIT-STATE))))

(def clear!
  (proxy [ActionListener] []
          (actionPerformed [event]
            (swap! EDIT-STATE #(assoc %1 :table-json-struct EMPTY-TABLE))
            (refresh!))))

(def edit-config!
  (proxy [ActionListener] []
          (actionPerformed [event]

            )))

(defn- get-toolbar
  []
  (let [bar (JMenuBar.)
        file (JMenu. "File")
        edit (JMenu. "Edit")
        write (JMenuItem. "Write current table")
        clear (JMenuItem. "Clear current table")
        analytics (JMenuItem. "Add Analytic")
        add-cue (JMenuItem. "Add Cue")
        add-ball (JMenuItem. "Add Ball")
        deselect (JMenuItem. "Deselect")
        edit-config (JMenuItem. "Edit Config")]
        (do
          (.add file write)
          (.addActionListener write write-current!)
          (.add file clear)
          (.addActionListener clear clear!)
          (.add edit add-cue)
          (.addActionListener add-cue add-new-cue!)
          (.add edit add-ball)
          (.addActionListener add-ball (add-new-ball! :solid))
          (.add edit deselect)
          (.addActionListener deselect deselect!)
          (.addSeparator edit)
          (.add edit edit-config)
          (.addActionListener edit-config edit-config!)
          (doto bar
            (.add file)
            (.add edit)))))

(defn edit-tables
  "open graphical window for creating a new
  test state"
  [filename]
  (let [edit-window (JFrame. config/EDIT-WINDOW-TITLE)
        table-panel (static-panel! config/POOL-WIDTH-PX config/POOL-HEIGHT-PX)]
      (do (set-new-es! filename)
          (reset! GRAPHICS-PANEL table-panel)
          (doto table-panel
            (.setPreferredSize
              (Dimension. config/POOL-WIDTH-PX (+ config/POOL-HEIGHT-PX 20)))
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
