(ns hexenhammer.core
  (:require [clojure.string :as str]
            [hiccup.core :refer [html]]
            [hexenhammer.svg.core :as svg]
            [hexenhammer.cube :as cube]))



(spit "index.html"
      (html (svg/render-state {:map/rows 8
                               :map/columns 12
                               :map/units {(cube/->Cube 6 1 -7) {:unit/name "warrior"
                                                                 :unit/id "i"
                                                                 :unit/models 12
                                                                 :unit/facing :n}}})))
