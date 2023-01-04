(ns hexenhammer.core
  (:require [clojure.string :as str]
            [hiccup.core :refer [html]]
            [hexenhammer.svg.core :as svg]))



(spit "index.html"
      (html [:html
             [:head]
             [:body
              (svg/render-state {:size 2})]]))
