(ns hexenhammer.core
  (:require [clojure.string :as str]
            [hiccup.core :refer [html]]
            [hexenhammer.svg.core :as svg]))



(spit "index.html"
      (html (svg/render-state {:map/size 2
                               :map/units {[0 0 0] {:unit/name "warrior"
                                                    :unit/id 1
                                                    :unit/models 12
                                                    :unit/facing :n}}})))
