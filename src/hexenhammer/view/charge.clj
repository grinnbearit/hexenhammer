(ns hexenhammer.view.charge
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
       [:h2 "Charge"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state)]])))


(defn pick-targets
  [state]
  (let [player (:game/player state)
        cube (:game/cube state)
        unit (get-in state [:game/battlefield cube])
        charge-prob (sort (lp/charge (:unit/M unit)))]
    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 (str "Charge - Pick Targets")]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state)
       (r/render-profile unit) [:br]

       [:table
        [:thead
         [:th "Charge Range"]
         [:th "Success %"]]
        [:tbody
         (for [[hexes prob] charge-prob
               :let [perc (Math/round (float (* 100 prob)))]]
           [:tr
            [:td hexes]
            [:td (format "~%d%%" perc)]])]]
       [:br]

       [:form {:action "/charge/skip-charge" :method "post"}
        [:input {:type "submit" :value "Skip Charge"}]]]])))


(defn declare-targets
  [{:keys [game/charge] :as state}]
  (let [player (:game/player state)
        cube (:game/cube state)
        unit (get-in state [:game/battlefield cube])
        {:keys [events charge-range]} charge
        charge-prob (lp/charge (:unit/M unit) charge-range)
        charge-perc (Math/round (float (* 100 charge-prob)))]

    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 "Charge - Declare Targets"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state) [:br] [:br]
       (r/render-profile unit) [:br]
       (r/render-events events) [:br]
       [:form {:action "/charge/skip-charge" :method "post"}
        [:input {:type "submit" :value "Skip Charge"}]
        [:input {:type "submit" :value (format "Declare Targets (~%d%%)" charge-perc)
                 :formaction "/charge/declare-targets" :disabled true}]]]])))


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
