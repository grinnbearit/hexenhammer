(ns hexenhammer.svg.core
  (:require [hexenhammer.svg.internal :as int]))

;; Using the q, r, s coordinate system from https://www.redblobgames.com/grids/hexagons/


(defn render-grass
  "Returns a green hex with the coordinates printed"
  [size q r s]
  (seq
   [(int/svg-hexagon size q r s :fill "green")
    (int/svg-coordinates size q r s)]))


(defn render-unit
  "Returns a unit hex with the unit name, model count and unit id"
  [size q r s unit]
  (seq
   [(int/svg-hexagon size q r s :fill "#8b0000")
    (int/svg-unit size q r s (unit :unit/name) (unit :unit/id) (unit :unit/models))
    (int/svg-facing size q r s (unit :unit/facing))]))


(defn gen-grass-render-map
  "given a size, returns a map of (q r s) -> grass-render-fn"
  [size]
  (->> (for [q (range (- size) (inc size))
             r (range (- size) (inc size))
             s (range (- size) (inc size))
             :when (zero? (+ q r s))]
         [[q r s] (partial render-grass size q r s)])
       (into {})))


(defn gen-unit-render-map
  "given a unit list, returns a map of (q r s) -> unit-render-fn"
  [size units]
  (->> (for [[[q r s] unit] units]
         [[q r s] (partial render-unit size q r s unit)])
       (into {})))


(defn state->render-map
  "converts a state to a map of (q r s) -> render-fn"
  [state]
  (let [{:keys [map/size map/units]} state]
    (merge (gen-grass-render-map size)
           (gen-unit-render-map size units))))


(defn render-state
  "returns a hiccup svg datastructure representing the game state"
  [state]
  [:html
   [:head]
   [:body
    (let [{:keys [map/size]} state]
      [:svg (int/size->dim size)
       (for [[_ render-fn] (state->render-map state)]
         (render-fn))])]])
