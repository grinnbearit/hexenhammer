(ns hexenhammer.controller.setup-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.transition.core :as t]
            [hexenhammer.controller.setup :refer :all]))


(facts
 "select hex"

 (let [state {}]

   (select-hex state :cube-1)
   => {:game/battlemap :battlemap-2}

   (provided
    (t/reset-battlemap {:game/phase [:setup :add-unit]
                        :game/selected :cube-1}
                       [:cube-1])
    => {:game/battlemap :battlemap-1}

    (t/set-presentation :battlemap-1 [:cube-1] :selected)
    => :battlemap-2)))



(facts
 "add unit"

 (let [state {:game/selected :cube-1}]

   (add-unit state :cube-1)
   => {:game/battlemap :battlemap-2}

   (provided
    (t/reset-battlemap {:game/phase [:setup :select-hex]})
    => {:game/battlemap :battlemap-1}

    (t/set-presentation :battlemap-1 :silent-selectable)
    => :battlemap-2)))
