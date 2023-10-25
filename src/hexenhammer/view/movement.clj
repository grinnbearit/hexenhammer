(ns hexenhammer.view.movement
  (:require [hiccup.core :refer [html]]
            [hexenhammer.web.css :refer [STYLESHEET]]
            [hexenhammer.render.bit :as rb]
            [hexenhammer.render.core :as rc]))


(defn select
  [state]
  (let [player (:game/player state)]
    (html
     [:html
      [:head
       [:h1 (rb/player->str (:game/player state))]
       [:h2 "Movement"]
       [:style STYLESHEET]]
      [:body
       (rc/render-battlefield state)]])))
