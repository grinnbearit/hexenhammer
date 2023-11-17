(ns hexenhammer.view.charge.reaction
  (:require [hiccup.core :refer [html]]
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


(defn react
  [state]
  (let [player (:game/player state)
        cube (:game/cube state)
        unit (get-in state [:game/battlefield cube])]
    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 "Charge - Reaction - React"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state)
       (r/render-profile unit) [:br]]])))
