(ns hexenhammer.core
  (:require [hiccup.core :refer [html]]
            [hexenhammer.cube :as cube]
            [hexenhammer.unit :as unit]
            [hexenhammer.svg.core :as svg]
            [hexenhammer.transition :as transition]))


(def hexenhammer-state (atom nil))
(transition/!init-state hexenhammer-state 8 12)
(transition/!place-unit hexenhammer-state
                        (cube/->Cube 6 1 -7)
                        (unit/gen-warrior "i" :facing :n))


(spit "index.html"
      (html (svg/render-state @hexenhammer-state)))
