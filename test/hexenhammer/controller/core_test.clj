(ns hexenhammer.controller.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.transition.core :as t]
            [hexenhammer.controller.core :refer :all]))


(facts
 "to setup"

 (to-setup {})
 => {:game/battlemap :battlemap-2}

 (provided
  (t/reset-battlemap {:game/phase [:setup :select-hex]})
  => {:game/battlemap :battlemap-1}

  (t/set-presentation :battlemap-1 :silent-selectable)
  => :battlemap-2))
