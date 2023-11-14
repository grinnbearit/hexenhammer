(ns hexenhammer.controller.charge-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.battlemap :as tsb]
            [hexenhammer.controller.charge :refer :all]))


(facts
 "unselect"

 (unselect {:game/charge {:charger-keys [:unit-key-1 :unit-key-2]
                          :charger-cubes [:cube-1 :cube-2]
                          :cube->enders {}}
            :game/cube :cube-1})
 => {:game/battlemap :battlemap-2}

 (provided
  (tsb/reset-battlemap {:game/charge {:charger-keys [:unit-key-1 :unit-key-2]
                                      :charger-cubes [:cube-1 :cube-2]}
                        :game/phase [:charge :select-hex]}
                       [:cube-1 :cube-2])
  => {:game/battlemap :battlemap-1}

  (tb/set-presentation :battlemap-1 :selectable)
  => :battlemap-2)


 (unselect {:game/charge {:charger-cubes []}
            :game/cube :cube-1
            :game/battlemap :battlemap-1})
 => {:game/charge {:charger-cubes []}
     :game/phase [:charge :to-movement]})


(facts
 "select hex"

 (select-hex {} :cube-1)
 => {:game/battlemap :battlemap-2}

 (provided
  (tsb/reset-battlemap {:game/cube :cube-1
                        :game/phase [:charge :skip-charge]}
                       [:cube-1])
  => {:game/battlemap :battlemap-1}

  (tb/set-presentation :battlemap-1 :selected)
  => :battlemap-2))


(facts
 "select skip"

 (select-skip :state-1 :cube-1)
 => :unselect

 (provided
  (unselect :state-1) => :unselect))


(facts
 "skip charge"

 (let [state {:game/cube :cube-1
              :game/battlefield :battlefield-1
              :game/charge {:charger-keys #{:unit-key-1 :unit-key-2}
                            :charger-cubes #{:cube-1 :cube-2}}}]

   (skip-charge state)
   => :unselect

   (provided
    (lbu/unit-key :battlefield-1 :cube-1) => :unit-key-1

    (unselect {:game/cube :cube-1
               :game/battlefield :battlefield-1
               :game/charge {:charger-keys #{:unit-key-2}
                             :charger-cubes #{:cube-2}}})
    => :unselect)))
