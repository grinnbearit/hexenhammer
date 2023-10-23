(ns hexenhammer.view.setup
  (:require [hiccup.core :refer [html]]
            [hexenhammer.web.css :refer [STYLESHEET]]
            [hexenhammer.render.core :as rc]))


(defn select
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Setup - Select Hex"]
     [:style STYLESHEET]
     [:body
      (rc/render-battlefield state)]]]))


(defn add-unit
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Setup - Add Unit"]
     [:style STYLESHEET]]
    [:body
     (rc/render-battlefield state) [:br] [:br]

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
            [:option {:value facing-code} facing-name])]]]
       [:tr
        [:td
         "Profile"]
        [:td
         [:table
          [:thead
           [:th [:label {:for "M"} "M"]]
           [:th [:label {:for "Ld"} "Ld"]]
           [:th [:label {:for "R"} "R"]]]
          [:tbody
           [:tr
            [:td [:input {:type "number" :id "M" :name "M" :min 1 :max 10 :step 1 :value 4}]]
            [:td [:input {:type "number" :id "Ld" :name "Ld" :min 1 :max 10 :step 1 :value 7}]]
            [:td [:input {:type "number" :id "R" :name "R" :min 1 :max 4 :step 1 :value 4}]]]]]]]]

      [:input {:type "submit" :value "Add Unit"}]]]]))


(defn remove-unit
  [state]
  (let [cube (:game/selected state)
        unit (get-in state [:game/battlefield cube])]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Setup - Remove Unit"]
       [:style STYLESHEET]]
      [:body
       (rc/render-battlefield state)
       (rc/render-profile unit) [:br]

       [:form {:action "/setup/remove-unit" :method "post"}
        [:input {:type "submit" :value "Remove Unit"}]]]])))
