(ns hexenhammer.view.charge
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
       [:h2 "Charge"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state)]])))


(defn skip-charge
  [state]
  (let [player (:game/player state)
        cube (:game/cube state)
        unit (get-in state [:game/battlefield cube])]
    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 (str "Charge - Skip Charge")]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state)
       (r/render-profile unit) [:br]

       [:form {:action "/charge/skip-charge" :method "post"}
        [:input {:type "submit" :value "Skip Charge"}]]]])))


(defn to-movement
  [state]
  (let [player (:game/player state)]
    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 "Charge"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state) [:br] [:br]
       [:form {:action "/to-movement" :method "post"}
        [:input {:type "submit" :value "To Movement"}]]]])))
