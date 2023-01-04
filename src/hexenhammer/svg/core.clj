(ns hexenhammer.svg.core
  (:require [hexenhammer.svg.internal :as int]))

;; Using the q, r, s coordinate system from https://www.redblobgames.com/grids/hexagons/


(defn render-state
  "returns a hiccup svg datastructure representing the game state"
  [state]
  (let [{:keys [size]} state]
    [:svg (int/size->dim size)
     (for [q (range (- size) (inc size))
           r (range (- size) (inc size))
           s (range (- size) (inc size))
           :when (zero? (+ q r s))]
       (int/svg-hexagon size q r s))]))
