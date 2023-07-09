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


(defmulti render :game/phase)


(defmethod render :setup
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Setup"]
     [:style STYLESHEET]]
    (let [{:keys [game/rows game/columns game/battlefield]} state]
      [:body

       ;; Battlefield
       [:svg (svg/size->dim rows columns)
        (for [entity (sort-by entity->z (vals battlefield))]
          (entity/render entity))]])]))
