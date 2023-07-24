(ns hexenhammer.view.html
  (:require [garden.core :refer [css]]
            [hiccup.core :refer [html]]
            [hexenhammer.view.svg :as svg]
            [hexenhammer.view.entity :as entity]))


(def STYLESHEET
  (css
   [:polygon
    [:&.terrain {:fill "#6aa84f" :stroke "black"}
     [:&.selected {:stroke "yellow"}]]
    [:&.unit
     [:&.player-1 {:fill "#990000" :stroke "black"}
      [:&.selected {:stroke "yellow"}]]
     [:&.player-2 {:fill "#1155cc" :stroke "black"}
      [:&.selected {:stroke "yellow"}]]]]
   [:table :th :td {:border "1px solid"}]))


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
     (render-battlefield state)
     [:form {:action "/setup/to-movement" :method "post"}
      [:input {:type "submit" :value "To Movement"}]]]]))


(defmethod render [:setup :add-unit]
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Setup - Add Unit"]
     [:style STYLESHEET]]
    [:body
     (render-battlefield state) [:br] [:br]
     [:form {:action "/setup/add-unit" :method "post"}
      [:table
       [:tr
        [:td
         [:label {:for "player"} "Player"]]
        [:td
         [:input {:type "radio" :id "player" :name "player" :value "1" :checked true} "1"]
         [:input {:type "radio" :name "player" :value "2"} "2"]]]
       [:tr
        [:td
         [:label {:for "facing"} "Facing"]]
        [:td
         [:select {:id "facing" :name "facing"}
          (for [[facing-code facing-name]
                [["n" "North"] ["ne" "North-East"] ["se" "South-East"]
                 ["s" "South"] ["sw" "South-West"] ["nw" "North-West"]]]
            [:option {:value facing-code} facing-name])]]]]
      [:input {:type "submit" :value "Add Unit"}]]]]))


(defmethod render [:setup :remove-unit]
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Setup - Remove Unit"]
     [:style STYLESHEET]]
    [:body
     (render-battlefield state) [:br] [:br]
     [:form {:action "/setup/remove-unit" :method "post"}
      [:input {:type "submit" :value "Remove Unit"}]]]]))


(defmethod render [:movement :select-hex]
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 (str "Player - " (:game/player state))]
     [:h3 "Movement"]
     [:style STYLESHEET]]
    [:body
     (render-battlefield state)]]))
