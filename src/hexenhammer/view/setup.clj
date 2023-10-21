(ns hexenhammer.view.setup
  (:require [hiccup.core :refer [html]]
            [hexenhammer.web.css :refer [STYLESHEET]]))


(defn select
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Setup - Select Hex"]
     [:style STYLESHEET]]]))
