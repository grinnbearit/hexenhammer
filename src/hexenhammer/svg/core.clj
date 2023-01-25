(ns hexenhammer.svg.core
  (:require [hexenhammer.svg.internal :as int]
            [hexenhammer.cube :as cube]))

;; Using the q, r, s coordinate system from https://www.redblobgames.com/grids/hexagons/


(defn svg-unit
  "Writes unit information on the hex"
  [size cube unit]
  (int/svg-translate
   size cube
   [:g {}
    (int/svg-hexagon :fill "#8b0000")
    (int/svg-text -1 (format "%s" (:unit/name unit)))
    (int/svg-text 0 (format "%s" (:unit/id unit)))
    (int/svg-text 1 (format "(%d)" (:unit/models unit)))
    (int/svg-chevron (:unit/facing unit))]))


(defn svg-grass
  "Returns a green hex with the coordinates printed"
  [size cube]
  (int/svg-translate
   size cube
   [:g {}
    (int/svg-hexagon :fill "green")
    (int/svg-coordinates cube)]))


(defn state->svg
  "converts a state to an svg datastructure representing the map"
  [state]
  (let [{:keys [map/size map/units]} state]
    (for [q (range (- size) (inc size))
          r (range (- size) (inc size))
          s (range (- size) (inc size))
          :when (zero? (+ q r s))
          :let [cube (cube/->Cube q r s)]]
      (if-let [unit (units cube)]
        (svg-unit size cube unit)
        (svg-grass size cube)))))


(defn render-state
  "returns a hiccup svg datastructure representing the game state"
  [state]
  [:html
   [:head]
   [:body
    (let [{:keys [map/size]} state]
      [:svg (int/size->dim size)
       (state->svg state)])]])
