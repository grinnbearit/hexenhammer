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


(defn heavy-casualties-passed
  [state]
  (let [{:keys [roll unit-cube]} (:game/event state)
        unit (get-in state [:game/battlefield unit-cube])]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Event - Heavy Casualties"]
       [:style STYLESHEET]
       [:body
        (r/render-battlefield state) [:br] [:br]
        (r/render-profile unit) [:br]
        (r/render-events (:game/events state)) [:br]
        [:h3 "Passed!"]
        (rs/dice roll 1)
        [:form {:action "/event/trigger" :method "post"}
         [:input {:type "submit" :value "Next"}]]]]])))


(defn heavy-casualties-failed
  [state]
  (let [{:keys [roll unit-cube]} (:game/event state)
        unit (get-in state [:game/battlefield unit-cube])]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Event - Heavy Casualties"]
       [:style STYLESHEET]
       [:body
        (r/render-battlefield state) [:br] [:br]
        (r/render-profile unit) [:br]
        (r/render-events (:game/events state)) [:br]
        [:h3 "Failed!"]
        (rs/dice roll 7)
        [:form {:action "/event/heavy-casualties/flee" :method "post"}
         [:input {:type "submit" :value "Flee!"}]]]]])))


(defn heavy-casualties-flee
  [state]
  (let [{:keys [edge? unit roll]} (:game/event state)]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Event - Heavy Casualties - Flee!"]
       [:style STYLESHEET]
       [:body
        (r/render-battlefield state) [:br] [:br]
        (r/render-profile unit) [:br]
        (r/render-events (:game/events state)) [:br]
        (rs/dice roll)
        [:form {:action "/event/trigger" :method "post"}
         [:input {:type "submit" :value "Next"}]]]]])))
