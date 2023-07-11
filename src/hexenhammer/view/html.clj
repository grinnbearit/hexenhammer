(ns hexenhammer.view.html
  (:require [garden.core :refer [css]]
            [hiccup.core :refer [html]]
            [hexenhammer.view.svg :as svg]
            [hexenhammer.view.entity :as entity]))


(def STYLESHEET
  (css
   [:polygon
    [:&.terrain {:fill "#6aa84f" :stroke "black"}
     [:&.selected {:stroke "yellow"}]]]))


(defn entity->z
  "Returns the z index value for the passed entity depending on the presentation status"
  [entity]
  (let [presentation->rank {:default 0 :selected 1}]
    (-> (:entity/presentation entity)
        (presentation->rank))))


(defn render-battlefield
  [{:keys [game/rows game/columns game/battlefield]}]
  [:svg (svg/size->dim rows columns)
   (for [entity (sort-by entity->z (vals battlefield))]
     (entity/render entity))])


(defmulti render (juxt :game/phase :game/subphase))


(defmethod render [:setup :select-hex]
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Setup"]
     [:style STYLESHEET]]
    [:body
     (render-battlefield state)]]))


(defmethod render [:setup :add-unit]
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Setup - Add Unit"]
     [:style STYLESHEET]]
    [:body
     (render-battlefield state)
     [:h2 "Add Unit"]]]))
