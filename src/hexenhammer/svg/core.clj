(ns hexenhammer.svg.core
  (:require [hexenhammer.svg.internal :as int]
            [hexenhammer.cube :as cube]))

;; Using the q, r, s coordinate system from https://www.redblobgames.com/grids/hexagons/

(defn svg-unit
  "Writes unit information on the hex"
  [unit]
  [:g {}
   (int/svg-hexagon :fill "#8b0000")
   (int/svg-text -1 (format "%s" (:unit/name unit)))
   (int/svg-text 0 (format "%s" (:unit/id unit)))
   (int/svg-text 1 (format "(%d)" (:unit/models unit)))
   (int/svg-chevron (:unit/facing unit))])


(defn svg-grass
  "Returns a green hex with the coordinates printed"
  [cube]
  [:g {}
   (int/svg-hexagon :fill "green")
   (int/svg-coordinates cube)])


(defn state->svg
  "converts a state to an svg datastructure representing the map"
  [{:keys [map/battlefield map/units]}]
  (for [[cube hex-obj] battlefield]
    (int/svg-translate
     cube
     (case (:hexenhammer/class hex-obj)
       :terrain (svg-grass cube)
       :unit (svg-unit hex-obj)))))


(defn render-state
  "returns a hiccup svg datastructure representing the game state"
  [state]
  [:html
   [:head]
   [:body
    (let [{:keys [map/rows map/columns]} state]
      [:svg (int/size->dim rows columns)
       (state->svg state)])]])
