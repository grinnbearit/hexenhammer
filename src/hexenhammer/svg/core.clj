(ns hexenhammer.svg.core
  (:require [hexenhammer.svg.internal :as int]
            [hexenhammer.svg.render :as render]
            [hexenhammer.cube :as cube]
            [hiccup.core :refer [html]]
            [garden.core :refer [css]]))

;; Using the q, r, s coordinate system from https://www.redblobgames.com/grids/hexagons/


(defn state->svg
  "converts a state to an svg datastructure representing the map"
  [{:keys [map/battlefield]}]
  (for [[cube hex-obj] battlefield]
    (int/svg-translate
     cube
     (case (:hexenhammer/class hex-obj)
       :terrain (render/svg-terrain cube)
       :unit (render/svg-unit hex-obj)))))


(defn render-state
  "returns a hiccup svg datastructure representing the game state"
  [state]
  (html
   [:html
    [:head
     [:style (css [:polygon
                   [:&.grass {:fill "green" :stroke "black"}]
                   [:&.unit {:fill "red" :stroke "black"}]])]]
    [:body
     (let [{:keys [map/rows map/columns]} state]
       [:svg (int/size->dim rows columns)
        (state->svg state)])]]))
