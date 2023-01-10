(ns hexenhammer.svg.core
  (:require [hexenhammer.svg.internal :as int]))

;; Using the q, r, s coordinate system from https://www.redblobgames.com/grids/hexagons/


(defn render-grass
  "Returns a green hex with the coordinates printed"
  [size q r s]
  (seq
   [(int/svg-hexagon size q r s :colour "green")
    (int/svg-coordinates size q r s)]))


(defn render-state
  "returns a hiccup svg datastructure representing the game state"
  [state]
  (let [{:keys [size]} state]
    [:svg (int/size->dim size)
     (for [q (range (- size) (inc size))
           r (range (- size) (inc size))
           s (range (- size) (inc size))
           :when (zero? (+ q r s))]
       (render-grass size q r s))]))
