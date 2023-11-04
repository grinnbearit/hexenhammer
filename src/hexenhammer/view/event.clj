(ns hexenhammer.view.event
  (:require [hiccup.core :refer [html]]
            [hexenhammer.render.core :as rc]
            [hexenhammer.web.css :refer [STYLESHEET]]))


(defn dangerous
  [state]
  (let [events (:game/events state)]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Event - Dangerous Terrain"]
       [:style STYLESHEET]
       [:body
        (rc/render-battlefield state) [:br] [:br]
        (rc/render-events events) [:br]
        [:form {:action "/event/trigger" :method "post"}
         [:input {:type "submit" :value "Next"}]]]]])))
