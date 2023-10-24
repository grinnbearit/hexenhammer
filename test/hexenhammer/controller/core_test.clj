(ns hexenhammer.controller.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.controller.core :refer :all]))


(facts
 "to setup"

 (to-setup {})
 => {:game/battlemap :battlemap-2}

 (provided
  (tb/reset-battlemap {:game/phase [:setup :select-hex]})
  => {:game/battlemap :battlemap-1}

  (tb/set-presentation :battlemap-1 :silent-selectable)
  => :battlemap-2))
