(ns hexenhammer.core
  (:require [hexenhammer.cube :as cube]
            [hexenhammer.unit :as unit]
            [hexenhammer.svg.core :as svg]
            [hexenhammer.transition :as transition]))


(def hexenhammer-state (atom nil))
(reset! hexenhammer-state (transition/gen-initial-state 8 12))
(swap! hexenhammer-state transition/place-unit
       (cube/->Cube 6 1 -7)
       (unit/gen-warrior "i" :facing :n))


(spit "index.html" (svg/render-state @hexenhammer-state))
