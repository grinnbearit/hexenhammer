(ns hexenhammer.controller.setup-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.transition.core :as t]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.controller.setup :refer :all]))


(facts
 "select hex"

 (let [state {:game/battlefield {:cube-1 :terrain-1}}]

   (select-hex state :cube-1)
   => {:game/battlemap :battlemap-2}

   (provided
    (let/terrain? :terrain-1) => true

    (t/reset-battlemap {:game/battlefield {:cube-1 :terrain-1}
                        :game/phase [:setup :add-unit]
                        :game/cube :cube-1}
                       [:cube-1])
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 [:cube-1] :selected)
    => :battlemap-2))


 (let [state {:game/battlefield {:cube-1 :unit-1}}]

   (select-hex state :cube-1)
   => {:game/battlemap :battlemap-2}

   (provided
    (let/terrain? :unit-1) => false

    (t/reset-battlemap {:game/battlefield {:cube-1 :unit-1}
                        :game/phase [:setup :remove-unit]
                        :game/cube :cube-1}
                       [:cube-1])
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 [:cube-1] :selected)
    => :battlemap-2)))



(facts
 "unselect"

 (let [state {:game/cube :cube-1}]

   (unselect state)
   => {:game/battlemap :battlemap-2}

   (provided
    (t/reset-battlemap {:game/phase [:setup :select-hex]})
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 :silent-selectable)
    => :battlemap-2)))


(facts
 "select add unit"

 (select-add-unit :state :cube-1)
 => :unselect

 (provided
  (unselect :state) => :unselect))


(facts
 "select remove unit"

 (select-remove-unit :state :cube-1)
 => :unselect

 (provided
  (unselect :state) => :unselect))


(facts
 "add unit"

 (let [state {:game/cube :cube-1
              :game/units :units-1
              :game/battlefield {:cube-1 :terrain-1}}]

   (add-unit state 1 :n 2 3 4)
   => :unselect

   (provided
    (tu/next-id :units-1 1 "infantry") => 1
    (leu/gen-infantry 1 1 :n 2 3 4) => :unit-1
    (leu/unit-key :unit-1) => :unit-key-1
    (let/place :terrain-1 :unit-1) => :unit-2
    (tu/inc-id :units-1 1 "infantry") => :units-2
    (tu/set-unit :units-2 :unit-key-1 :cube-1) => :units-3

    (unselect {:game/cube :cube-1
               :game/battlefield {:cube-1 :unit-2}
               :game/units :units-3})
    => :unselect)))


(facts
 "remove unit"

 (let [battlefield {:cube-1 :unit-1}
       state {:game/cube :cube-1
              :game/units :units-1
              :game/battlefield battlefield}]

   (remove-unit state)
   => :unselect

   (provided
    (lbu/unit-key battlefield :cube-1) => :unit-key-1

    (lbu/remove-unit battlefield :cube-1)
    => {:cube-1 :terrain-1}

    (tu/remove-unit :units-1 :unit-key-1) => :units-2

    (unselect {:game/cube :cube-1
               :game/units :units-2
               :game/battlefield {:cube-1 :terrain-1}})
    => :unselect)))


(facts
 "swap terrain"

 (swap-terrain {:game/cube :cube-1
                :game/battlefield {:cube-1 :entity-1}}
               :open)
 => :unselect

 (provided
  (let/terrain? :entity-1) => true

  (unselect {:game/cube :cube-1
             :game/battlefield {:cube-1 let/OPEN-GROUND}})
  => :unselect)


 (swap-terrain {:game/cube :cube-1
                :game/battlefield {:cube-1 :entity-1}}
               :dangerous)
 => :unselect

 (provided
  (let/terrain? :entity-1) => true

  (unselect {:game/cube :cube-1
             :game/battlefield {:cube-1 let/DANGEROUS-TERRAIN}})
  => :unselect)


 (swap-terrain {:game/cube :cube-1
                :game/battlefield {:cube-1 :entity-1}}
               :impassable)
 => :unselect

 (provided
  (let/terrain? :entity-1) => true

  (unselect {:game/cube :cube-1
             :game/battlefield {:cube-1 let/IMPASSABLE-TERRAIN}})
  => :unselect)


 (swap-terrain {:game/cube :cube-1
                :game/battlefield {:cube-1 :entity-1}}
               :open)
 => :unselect

 (provided
  (let/terrain? :entity-1) => false

  (let/place let/OPEN-GROUND :entity-1) => :entity-2

  (unselect {:game/cube :cube-1
             :game/battlefield {:cube-1 :entity-2}})
  => :unselect))
