(ns hexenhammer.core
  (:require [clojure.string :as str]
            [hiccup.core :refer [html]]
            [hexenhammer.svg.core :as svg]
            [hexenhammer.cube :as cube]))



(spit "index.html"
      (html (svg/render-state {:map/size 5
                               :map/units {(cube/->Cube 0 0 0) {:unit/name "warrior"
                                                                :unit/id "i"
                                                                :unit/models 12
                                                                :unit/facing :n}}})))
