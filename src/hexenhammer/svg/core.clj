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


(defn gen-battlefield-cubes
  "Returns a list of cube coordinates for a battlefield of size rows x columns"
  [rows columns]
  (let [hop-right (partial cube/cube-add (cube/->Cube 2 -1 -1))
        hop-down (partial cube/cube-add (cube/->Cube 0 1 -1))]
    (->> (interleave (iterate hop-right (cube/->Cube 0 0 0))
                     (iterate hop-right (cube/->Cube 1 0 -1)))
         (take columns)
         (iterate #(map hop-down %))
         (take rows)
         (apply concat))))


(defn state->svg
  "converts a state to an svg datastructure representing the map"
  [{:keys [map/rows map/columns map/units]}]
  (for [cube (gen-battlefield-cubes rows columns)]
    (int/svg-translate
     cube
     (if-let [unit (units cube)]
       (svg-unit unit)
       (svg-grass cube)))))


(defn render-state
  "returns a hiccup svg datastructure representing the game state"
  [state]
  [:html
   [:head]
   [:body
    (let [{:keys [map/rows map/columns]} state]
      [:svg (int/size->dim rows columns)
       (state->svg state)])]])
