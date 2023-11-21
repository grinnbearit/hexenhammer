(ns hexenhammer.view.charge.reaction
  (:require [hiccup.core :refer [html]]
            [hexenhammer.logic.probability :as lp]
            [hexenhammer.render.core :as r]
            [hexenhammer.render.bit :as rb]
            [hexenhammer.render.svg :as rs]
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


(defn fled
  [state]
  (let [player (:game/player state)
        cube (:game/cube state)
        unit (get-in state [:game/battlefield cube])]
    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 "Charge - Reaction - Fled"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state)
       (r/render-profile unit) [:br]
       [:form {:action "/charge/reaction/fled" :method "post"}
        [:input {:type "submit" :value "Flee"}]]]])))


(def flee-distance
  (let [flee-prob (sort lp/FLEE)]
    [:table
     [:thead
      [:th "Flee Distance"]
      [:th "Roll %"]]
     [:tbody
      (for [[hexes prob] flee-prob
            :let [perc (Math/round (float (* 100 prob)))]]
        [:tr
         [:td hexes]
         [:td (format "~%d%%" perc)]])]]))


(defn flee
  [state]
  (let [player (:game/player state)
        cube (:game/cube state)
        unit (get-in state [:game/battlefield cube])
        events (get-in state [:game/charge :events])]
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
       flee-distance [:br]

       [:form {:action "/charge/reaction/flee" :method "post"}
        [:input {:type "submit" :value "Flee"}]]
       [:table
        [:tr
         [:td [:a {:href "/charge/reaction/switch-reaction/hold"} "Hold"]]
         [:td "Flee"]]]]])))


(defn fleeing
  [state]
  (let [player (:game/player state)
        cube (:game/cube state)
        unit (get-in state [:game/battlefield cube])
        events (get-in state [:game/charge :events])]
    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 "Charge - Reaction - Fleeing"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state)
       (r/render-profile unit) [:br]
       (r/render-events events) [:br]
       flee-distance [:br]

       [:form {:action "/charge/reaction/fleeing" :method "post"}
        [:input {:type "submit" :value "Flee"}]]]])))


(defn flee-roll
  [state]
  (let [{:keys [edge? unit roll]} (:game/charge state)]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Charge - Reaction - Flee - Roll"]
       [:style STYLESHEET]
       [:body
        (r/render-battlefield state) [:br] [:br]
        (r/render-profile unit) [:br]
        (r/render-events (:game/events state)) [:br]
        (rs/dice roll)
        [:form {:action "/charge/reaction/trigger" :method "post"}
         [:input {:type "submit" :value "Next"}]]]]])))


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
