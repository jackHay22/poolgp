(ns poolgp.peripherals.testbuilder
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

(defrecord EditState [filename selected-ball selected-holder gs])
;
; (defn set-new-gs!
;   "reset gs atom with new blank state"
;   [filename]
;   (reset! EDIT-STATE
;     (EditState.
;       filename
;       nil ;selected ball
;       (utils/load-image (:holder resources/TABLE-IMAGES))
;       (utils/load-images
;         (GameState.
;           (Player. :p1 :genetic (list) 0 0 :unassigned)
;           (Player. :p2 :genetic (list) 0 0 :unassigned)
;           :p1
;           :p2
;           (list) ;balls
;           (list) ;pocketed
;           resources/TABLE
;           resources/CONTROLLER)))))
;
; (defn refresh! [] (.repaint @GRAPHICS-PANEL))
;
; (defn display-selected-ball
;   [gr edit-state]
;   (utils/draw-image gr 0 0 (:selected-holder edit-state))
;   (structs/render (:selected-ball edit-state) true gr))
;
; (defn render-builder-window
;   "render window components"
;   [gr edit-state]
;   (let [gs (:gs edit-state)]
;     (do
;       (utils/draw-image gr 0 0 (:surface (:table gs)))
;       (doall (map #(structs/render % true gr) (:balls gs)))
;       (utils/draw-image gr 0 0 (:raised (:table gs)))
;       (if (not (= nil (:selected-ball edit-state)))
;         (display-selected-ball gr edit-state)))))
;
; (defn add-ball-check-collisions!
;   "adds ball to state (true) or false"
;   [e]
;   (let [state @EDIT-STATE
;         ball-selected? (not (nil? (:selected-ball state)))]
;     (if ball-selected?
;         (let [current-balls (:balls (:gs state))
;               temp-position {:r config/BALL-RADIUS-PX
;                              :center (Vector. (.getX e) (.getY e))}
;               can-place? (reduce #(if (physics/ball-collision? temp-position %2)
;                                       (reduced false) %1)
;                                   true current-balls)]
;               (if can-place?
;                 (reset! EDIT-STATE
;                     (assoc
;                         (update-in state [:gs :balls]
;                            conj (assoc-in (:selected-ball state) [:center]
;                                  (Vector. (.getX e) (.getY e))))
;                         :selected-ball nil)))
;               can-place?)
;             false)))
;
; (defn static-panel!
;   "create a static, mouselistening panel"
;   [width height]
;   (let [base-image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
;         g (cast Graphics2D (.createGraphics base-image))]
;         (proxy [JPanel MouseListener] []
;                (mouseClicked [e]
;                  (if (add-ball-check-collisions! e) (refresh!)))
;                (mouseEntered [e])
;                (mouseExited [e])
;                (mousePressed [e])
;                (mouseReleased [e])
;                (paintComponent [^Graphics panel-graphics]
;                  (proxy-super paintComponent panel-graphics)
;                  (render-builder-window g @EDIT-STATE)
;                  (.drawImage panel-graphics base-image 0 0 width height nil)))))
;
; (defn select-ball!
;   "add a new ball to the selected-ball field
;   of the edit state and refresh graphics"
;   [id]
;   (do
;     (reset! EDIT-STATE
;         (assoc @EDIT-STATE :selected-ball
;           (Ball.
;             (Vector. 40 40)
;             config/BALL-RADIUS-PX
;             (Vector. 0 0)
;             config/BALL-MASS-G
;             id :solid
;             (utils/load-image
;               (id resources/BALL-IMAGES)))))
;       (refresh!)))
;
; (def add-new-cue!
;      (proxy [ActionListener] []
;              (actionPerformed [event]
;                 ;check if cue already added
;                 (let [cue-added (filter #(= (:id %) :cue)
;                                         (:balls (:gs @EDIT-STATE)))]
;                   (if (empty? cue-added)
;                       (select-ball! :cue))))))
;
; (def add-new-ball!
;       (proxy [ActionListener] []
;               (actionPerformed [event]
;                 (select-ball! :1)))) ;TODO
;
; (def deselect!
;       (proxy [ActionListener] []
;               (actionPerformed [event]
;                 (do
;                   (reset! EDIT-STATE
;                     (assoc @EDIT-STATE :selected-ball nil))
;                     (refresh!)))))
;
; (def save!
;     (proxy [ActionListener] []
;             (actionPerformed [event]
;               (let [edit-state @EDIT-STATE]
;                   (utils/write-state
;                     (:gs edit-state) (:filename edit-state))))))
;
; (defn get-toolbar
;   []
;   (let [bar (JMenuBar.)
;         file (JMenu. "File")
;         edit (JMenu. "Edit")
;         save (JMenuItem. "Save")
;         add-cue (JMenuItem. "Add Cue")
;         add-ball (JMenuItem. "Add Ball")
;         deselect (JMenuItem. "Deselect")]
;         (do
;           (.add file save)
;           (.addActionListener save save!)
;           (.add edit add-cue)
;           (.addActionListener add-cue add-new-cue!)
;           (.add edit add-ball)
;           (.addActionListener add-ball add-new-ball!)
;           (.add edit deselect)
;           (.addActionListener deselect deselect!)
;           (doto bar
;             (.add file)
;             (.add edit)))))
;
; (defn make-test
;   "open graphical window for creating a new
;   test state"
;   [filename]
;   (let [edit-window (JFrame. config/EDIT-WINDOW-TITLE)
;         table-panel (static-panel! config/POOL-WIDTH-PX config/POOL-HEIGHT-PX)]
;       (do (set-new-gs! filename)
;           (reset! GRAPHICS-PANEL table-panel)
;           (doto table-panel
;             (.setPreferredSize
;               (Dimension. config/POOL-WIDTH-PX (+ config/POOL-HEIGHT-PX 20)))
;             (.setFocusable true)
;             (.setLayout nil)
;             (.addMouseListener table-panel)
;             (.requestFocus))
;           (doto edit-window
;             (.add table-panel)
;             (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
;             (.setResizable false)
;             (.pack)
;             (.setJMenuBar (get-toolbar))
;             (.setVisible true)
;             (.validate)
;             (.repaint)))))
