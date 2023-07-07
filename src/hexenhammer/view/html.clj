(ns hexenhammer.view.html
  (:require [garden.core :refer [css]]
            [hiccup.core :refer [html]]
            [hexenhammer.view.svg :as svg]))


(def STYLESHEET
  (css
   [:polygon
    [:&.grass {:fill "#6aa84f" :stroke "black"} ;
     [:&.selected {:stroke "yellow"}]]
    [:&.unit
     [:&.player-1 {:fill "#990000" :stroke "black"}
      [:&.highlighted {:stroke "#cc0000"}]]
     [:&.player-2 {:fill "#1155cc" :stroke "black"}
      [:&.highlighted {:stroke "#efefef"}]]]]
   [:table :th :td {:border "1px solid"}]))


(defmulti render :game/phase)


(defmethod render :setup
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Setup"]
     [:style STYLESHEET]]
    (let [{:keys [map/rows map/columns map/battlefield]} state]
      [:body

       ;; Battlefield
       [:svg (svg/size->dim rows columns)]])]))
