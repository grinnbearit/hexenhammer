(ns hexenhammer.view.setup
  (:require [hiccup.core :refer [html]]
            [hexenhammer.web.css :refer [STYLESHEET]]
            [hexenhammer.render.core :as rc]))


(defn select
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Setup - Select Hex"]
     [:style STYLESHEET]
     [:body
      (rc/render-battlefield state)]]]))


(defn add-unit
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Setup - Add Unit"]
     [:style STYLESHEET]
     [:body
      (rc/render-battlefield state)]]]))
