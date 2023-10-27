(ns hexenhammer.view.movement
  (:require [hiccup.core :refer [html]]
            [hexenhammer.web.css :refer [STYLESHEET]]
            [hexenhammer.render.bit :as rb]
            [hexenhammer.render.core :as rc]))


(defn select-hex
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


(defn reform
  [state]
  (let [player (:game/player state)
        cube (:game/cube state)
        pointer (:game/pointer state)
        unit (get-in state [:game/battlefield cube])
        moved? (get-in state [:game/movement :moved?])
        events (get-in state [:game/movement :pointer->events pointer])]

    (html
     [:html
      [:head
       [:h1 (rb/player->str (:game/player state))]
       [:h2 (str "Movement - Reform")]
       [:style STYLESHEET]]
      [:body
       (rc/render-battlefield state)
       (rc/render-profile unit) [:br]
       (rc/render-events events) [:br]

       [:form {:action "/movement/skip-movement" :method "post"}
        [:input {:type "submit" :value "Skip Movement"}]

        (when moved?
          [:input {:type "submit" :value "Finish Movement"
                   :formaction "/movement/finish-movement"}])]

       [:table
        [:tr
         [:td "Forward"]
         [:td "Reposition"]
         [:td "March"]]]]])))
