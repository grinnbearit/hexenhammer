(ns hexenhammer.view.event
  (:require [hiccup.core :refer [html]]
            [hexenhammer.render.core :as r]
            [hexenhammer.render.bit :as rb]
            [hexenhammer.render.svg :as rs]
            [hexenhammer.web.css :refer [STYLESHEET]]))


(defn dangerous-terrain
  [state]
  (let [{:keys [unit-destroyed? models-destroyed roll unit]} (:game/event state)
        events (:game/events state)]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Event - Dangerous Terrain"]
       [:style STYLESHEET]
       [:body
        (r/render-battlefield state) [:br] [:br]
        (r/render-profile unit) [:br]
        (r/render-events events) [:br]
        (if unit-destroyed?
          [:h3 (str (rb/unit-key->str unit) " destroyed")]
          [:h3 (format "%d Models Destroyed" models-destroyed)])
        (rs/dice roll 2)
        [:form {:action "/event/trigger" :method "post"}
         [:input {:type "submit" :value "Next"}]]]]])))


(defn heavy-casualties
  [state]
  (let [events (:game/events state)]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Event - Heavy Casualties"]
       [:style STYLESHEET]
       [:body
        (r/render-battlefield state) [:br] [:br]
        (r/render-events events) [:br]
        [:form {:action "/event/trigger" :method "post"}
         [:input {:type "submit" :value "Next"}]]]]])))
