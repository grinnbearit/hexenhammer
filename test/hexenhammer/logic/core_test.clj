(ns hexenhammer.logic.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.logic.core :refer :all]))


(facts
 "battlefield visible?"

 (battlefield-visible? {:cube-1 {:entity/los 0}
                        :cube-2 {:entity/los 0}}
                       :cube-1 :cube-2)
 => true

 (provided
  (mc/cubes-between :cube-1 :cube-2) => [])


 (battlefield-visible? {:cube-1 {:entity/los 1}
                        :cube-2 {:entity/los 1}
                        :cube-3 {:entity/los 1}}
                       :cube-1 :cube-3)
 => false

 (provided
  (mc/cubes-between :cube-1 :cube-3) => [:cube-2])


 (battlefield-visible? {:cube-1 {:entity/los 1}
                        :cube-2 {:entity/los 0}
                        :cube-3 {:entity/los 1}}
                       :cube-1 :cube-3)
 => true

 (provided
  (mc/cubes-between :cube-1 :cube-3) => [:cube-2])


 (battlefield-visible? {:cube-1 {:entity/los 1}
                        :cube-2 {:entity/los 1}
                        :cube-3 {:entity/los 2}}
                       :cube-1 :cube-3)
 => true

 (provided
  (mc/cubes-between :cube-1 :cube-3) => [:cube-2]))


(facts
 "field of view"

 (field-of-view {:cube-1 {:unit/facing :n}} :cube-1)
 => []

 (provided
  (mc/forward-slice :cube-1 :n 1) => [])


 (field-of-view {:cube-1 {:unit/facing :n}} :cube-1)
 => []

 (provided
  (mc/forward-slice :cube-1 :n 1) => [:cube-2])


 (let [battlefield {:cube-1 {:unit/facing :n}
                    :cube-2 :entity-1}]

   (field-of-view battlefield :cube-1)
   => []

   (provided
    (mc/forward-slice :cube-1 :n 1) => [:cube-2]
    (battlefield-visible? battlefield :cube-1 :cube-2) => false))


 (let [battlefield {:cube-1 {:unit/facing :n}
                    :cube-2 :entity-1}]

   (field-of-view battlefield :cube-1)
   => [:cube-2]

   (provided
    (mc/forward-slice :cube-1 :n 1) => [:cube-2]
    (battlefield-visible? battlefield :cube-1 :cube-2) => true
    (mc/forward-slice :cube-1 :n 2) => [])))


(facts
 "remove unit"

 (let [battlefield {:cube-1 {:entity/class :unit}
                    :cube-2 :terrain-2}]

   (remove-unit battlefield :cube-1)
   => {:cube-1 :terrain-1
       :cube-2 :terrain-2}

   (provided
    (lt/pickup {:entity/class :unit}) => :terrain-1)))


(facts
 "move unit"

 (let [battlefield {:cube-1 {:entity/class :unit}
                    :cube-2 :terrain-2}]

   (move-unit battlefield :cube-1 (mc/->Pointer :cube-2 :n))
   => {:cube-1 :terrain-1
       :cube-2 :unit-2}

   (provided
    (lt/pickup {:entity/class :unit}) => :terrain-1

    (lt/place {:entity/class :unit
               :entity/cube :cube-2
               :unit/facing :n}
              :terrain-2)
    => :unit-2)))


(facts
 "set state"

 (set-state {:cube-1 {:entity/state :state-1}
             :cube-2 {:entity/state :state-2}
             :cube-3 {:entity/state :state-3}}
            :state-4)
 => {:cube-1 {:entity/state :state-4}
     :cube-2 {:entity/state :state-4}
     :cube-3 {:entity/state :state-4}}


 (set-state {:cube-1 {:entity/state :state-1}
             :cube-2 {:entity/state :state-2}
             :cube-3 {:entity/state :state-3}}
            [:cube-2 :cube-3]
            :state-4)
 => {:cube-1 {:entity/state :state-1}
     :cube-2 {:entity/state :state-4}
     :cube-3 {:entity/state :state-4}})


(facts
 "show cubes"

 (show-cubes {:cube-1 :entity-1
              :cube-2 :entity-2
              :cube-3 :entity-3}
             [:cube-1 :cube-2]
             :selected)
 => :set-state

 (provided
  (set-state {:cube-1 :entity-1
              :cube-2 :entity-2}
             :selected)
  => :set-state))
