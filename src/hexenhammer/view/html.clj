(ns hexenhammer.view.html
  (:require [garden.core :refer [css]]
            [hiccup.core :refer [html]]
            [hexenhammer.view.svg :as svg]
            [hexenhammer.view.entity :as entity]))


(def STYLESHEET
  (css
   [:polygon
    [:&.terrain {:fill "#6aa84f" :stroke "black"}]]))


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
       [:svg (svg/size->dim rows columns)
        (for [entity (vals battlefield)]
          (entity/render entity))]])]))
