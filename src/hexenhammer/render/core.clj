(ns hexenhammer.render.core
  (:require [hexenhammer.render.internal :as int]
            [hexenhammer.render.svg :as svg]
            [hexenhammer.cube :as cube]
            [hiccup.core :refer [html]]
            [garden.core :refer [css]]
            [ring.util.codec :refer [form-encode]]))

;; Using the q, r, s coordinate system from https://www.redblobgames.com/grids/hexagons/


(def STYLESHEET
  (css [:polygon
        [:&.grass {:fill "green" :stroke "black"}
         [:&.selected {:stroke "yellow"}]]
        [:&.unit {:fill "red" :stroke "black"}]]))


(defn render-state
  "Returns the html content for modifying the state"
  [state]
  (html
   [:html
    [:head
     [:style STYLESHEET]
     [:body
      (let [{:keys [map/rows map/columns map/battlefield]} state]
        [:svg (int/size->dim rows columns)
         (for [[cube hex-obj] battlefield]
           (int/svg-translate
            cube
            (case (:hexenhammer/class hex-obj)
              :terrain (svg/svg-terrain cube)
              :unit (svg/svg-unit hex-obj))))])
      [:a {:href "/modify"} "modify"]]]]))


(defn render-modify
  "Returns the html content for modifying the state"
  [state]
  (html
   [:html
    [:head
     [:style STYLESHEET]
     [:body
      (let [{:keys [map/rows map/columns map/battlefield map/selected]} state]
        [:svg (int/size->dim rows columns)
         (for [[cube hex-obj] battlefield]
           (int/svg-translate
            cube
            [:a {:href (str "/modify?" (form-encode cube))}
             (case (:hexenhammer/class hex-obj)
               :terrain (svg/svg-terrain cube)
               :unit (svg/svg-unit hex-obj))]))
         (when selected
           (int/svg-translate
            selected
            [:a {:href (str "/modify?" (form-encode selected))}
             (svg/svg-terrain selected :selected? true)]))])
      [:a {:href "/"} "play"]]]]))
