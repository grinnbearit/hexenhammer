(ns hexenhammer.view.charge.reaction
  (:require [hiccup.core :refer [html]]
            [hexenhammer.logic.probability :as lp]
            [hexenhammer.render.core :as r]
            [hexenhammer.render.bit :as rb]
            [hexenhammer.web.css :refer [STYLESHEET]]))


(defn select-hex
  [state]
  (let [player (:game/player state)]
    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 "Charge - Reaction"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state)]])))


(defn hold
  [state]
  (let [player (:game/player state)
        cube (:game/cube state)
        unit (get-in state [:game/battlefield cube])]
    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 "Charge - Reaction - Hold"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state)
       (r/render-profile unit) [:br]
       [:form {:action "/charge/reaction/hold" :method "post"}
        [:input {:type "submit" :value "Hold"}]]
       [:table
        [:tr
         [:td "Hold"]
         [:td [:a {:href "/charge/reaction/switch-reaction/flee"} "Flee"]]]]]])))


(defn flee
  [state]
  (let [player (:game/player state)
        cube (:game/cube state)
        unit (get-in state [:game/battlefield cube])
        events (get-in state [:game/charge :events])
        flee-prob (sort lp/FLEE)]
    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 "Charge - Reaction - Flee"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state)
       (r/render-profile unit) [:br]
       (r/render-events events) [:br]

       [:table
        [:thead
         [:th "Flee Distance"]
         [:th "Roll %"]]
        [:tbody
         (for [[hexes prob] flee-prob
               :let [perc (Math/round (float (* 100 prob)))]]
           [:tr
            [:td hexes]
            [:td (format "~%d%%" perc)]])]] [:br]

       [:form {:action "/charge/reaction/flee" :method "post"}
        [:input {:type "submit" :value "Flee" :disabled true}]]
       [:table
        [:tr
         [:td [:a {:href "/charge/reaction/switch-reaction/hold"} "Hold"]]
         [:td "Flee"]]]]])))


(defn finish-reaction
  [state]
  (let [player (:game/player state)]
    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 "Charge - Reaction"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state) [:br] [:br]
       [:form {:action "/charge/finish-reaction" :method "post"}
        [:input {:type "submit" :value "Finish Reaction"}]]]])))
