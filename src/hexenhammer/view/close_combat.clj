(ns hexenhammer.view.close-combat
  (:require [hiccup.core :refer [html]]
            [hexenhammer.render.core :as r]
            [hexenhammer.render.bit :as rb]
            [hexenhammer.web.css :refer [STYLESHEET]]))


(defn close-combat
  [state]
  (let [player (:game/player state)]
    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 "Close Combat"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state)]])))
