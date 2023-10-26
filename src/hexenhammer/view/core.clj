(ns hexenhammer.view.core
  (:require [hiccup.core :refer [html]]
            [hexenhammer.web.css :refer [STYLESHEET]]))


(defn select-hex
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:style STYLESHEET]
     [:body
      [:form {:action "/to-setup" :method "post"}
       [:input {:type "submit" :value "To Setup"}]]]]]))
