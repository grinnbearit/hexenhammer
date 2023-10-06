(ns hexenhammer.logic.terrain-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.model.unit :as mu]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.logic.unit :refer :all]))


(facts
 "enemies?"

 (enemies? {:unit/player 1}
           {:unit/player 1})
 => false

 (enemies? {:unit/player 1}
           {:unit/player 2})
 => true)



(facts
 "allies?"

 (allies? {:unit/player 1}
          {:unit/player 1})
 => true

 (allies? {:unit/player 1}
          {:unit/player 2})
 => false)


(facts
 "units engaged?"

 (engaged? :unit-1 :unit-2)
 => false

 (provided
  (enemies? :unit-1 :unit-2) => false)


 (engaged? {:unit/player 1
            :entity/cube :cube-1
            :unit/facing :n}
           {:unit/player 2
            :entity/cube :cube-2
            :unit/facing :n})
 => false

 (provided
  (mc/forward-arc :cube-1 :n)
  => [:cube-3 :cube-4]

  (mc/forward-arc :cube-2 :n)
  => [:cube-5 :cube-6])


 (engaged? {:unit/player 1
            :entity/cube :cube-1
            :unit/facing :n}
           {:unit/player 2
            :entity/cube :cube-2
            :unit/facing :n})
 => true

 (provided
  (mc/forward-arc :cube-1 :n)
  => [:cube-2])


 (engaged? {:unit/player 1
            :entity/cube :cube-1
            :unit/facing :n}
           {:unit/player 2
            :entity/cube :cube-2
            :unit/facing :n})
 => true

 (provided
  (mc/forward-arc :cube-1 :n)
  => [:cube-3 :cube-4]

  (mc/forward-arc :cube-2 :n)
  => [:cube-1]))


(facts
 "engaged cubes?"

 (engaged-cubes {:cube-1 :unit-1} :cube-1)
 => []

 (provided
  (mc/neighbours :cube-1) => [])


 (engaged-cubes {:cube-1 :unit-1} :cube-1)
 => []

 (provided
  (mc/neighbours :cube-1) => [:cube-2])


 (engaged-cubes {:cube-1 :unit-1
                 :cube-2 :terrain-1}
                :cube-1)
 => []

 (provided
  (mc/neighbours :cube-1) => [:cube-2]

  (le/unit? :terrain-1) => false)


 (engaged-cubes {:cube-1 :unit-1
                 :cube-2 :unit-2}
                :cube-1)
 => []

 (provided
  (mc/neighbours :cube-1) => [:cube-2]

  (le/unit? :unit-2) => true

  (engaged? :unit-1 :unit-2) => false)


 (engaged-cubes {:cube-1 :unit-1
                 :cube-2 :unit-2}
                :cube-1)
 => [:cube-2]

 (provided
  (mc/neighbours :cube-1) => [:cube-2]

  (le/unit? :unit-2) => true

  (engaged? :unit-1 :unit-2) => true))


(facts
 "battlefield engaged?"

 (battlefield-engaged? :battlefield :cube-1)
 => false

 (provided
  (engaged-cubes :battlefield :cube-1) => [])


 (battlefield-engaged? :battlefield :cube-1)
 => true

 (provided
  (engaged-cubes :battlefield :cube-1) => [:cube-2]))


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

 (let [battlefield {:cube-1 :unit-1
                    :cube-2 :terrain-2}]

   (remove-unit battlefield :cube-1)
   => {:cube-1 :terrain-1
       :cube-2 :terrain-2}

   (provided
    (lt/pickup :unit-1) => :terrain-1)))


(facts
 "move unit"

 (let [battlefield {:cube-1 {:entity/class :unit}
                    :cube-2 :terrain-2}]

   (move-unit battlefield :cube-1 (mc/->Pointer :cube-2 :n))
   => {:cube-1 :terrain-1
       :cube-2 :unit-2}

   (provided
    (remove-unit battlefield :cube-1)
    => {:cube-1 :terrain-1
        :cube-2 :terrain-2}

    (lt/swap :terrain-2 {:entity/class :unit
                         :unit/facing :n})
    => :unit-2)))


(facts
 "destroyed?"

 (destroyed? :unit 1) => false

 (provided
  (mu/wounds :unit) => 2)


 (destroyed? :unit 2) => true

 (provided
  (mu/wounds :unit) => 2))


(facts
 "damage unit"

 (damage-unit :unit-1 3)
 => :unit-2

 (provided
  (mu/wounds :unit-1) => 4

  (mu/set-wounds :unit-1 1) => :unit-2))


(facts
 "destroy models"

 (destroy-models :unit-1 2) => :unit-2

 (provided
  (mu/models :unit-1) => 3

  (mu/set-models :unit-1 1) => :unit-2))


(facts
 "phase reset"

 (let [unit-1 {:entity/name "unit-1"
               :unit/flags {:panicked? true}}
       unit-2 {:entity/name "unit-2"}
       unit-3 {:entity/name "terrain"}]

   (phase-reset {:cube-1 unit-1
                 :cube-2 unit-2
                 :cube-3 unit-3}
                [:cube-1 :cube-2])
   => {:cube-1 {:entity/name "unit-1"
                :unit/phase-strength 10
                :unit/flags {}}
       :cube-2 {:entity/name "unit-2"
                :unit/phase-strength 8
                :unit/flags nil}
       :cube-3 {:entity/name "terrain"}}

   (provided
    (mu/unit-strength unit-1) => 10
    (mu/unit-strength unit-2) => 8)))


(facts
 "panickable?"

 (let [battlefield {:cube-1 {:unit/flags {:panicked? true}}}]

   (panickable? battlefield :cube-1) => false)


 (let [battlefield {:cube-1 {:unit/flags {:fleeing? true}}}]

   (panickable? battlefield :cube-1) => false)


 (let [battlefield {:cube-1 {:unit/flags {}}}]

   (panickable? battlefield :cube-1) => false

   (provided
    (battlefield-engaged? battlefield :cube-1) => true))


 (let [battlefield {:cube-1 {:unit/flags {}}}]

   (panickable? battlefield :cube-1) => true

   (provided
    (battlefield-engaged? battlefield :cube-1) => false)))


(facts
 "heavy casualties?"

 (let [unit {:unit/phase-strength 16}
       battlefield {:cube-1 unit}]

   (heavy-casualties? battlefield :cube-1) => false

   (provided
    (mu/unit-strength unit) => 13)


   (heavy-casualties? battlefield :cube-1) => false

   (provided
    (mu/unit-strength unit) => 11
    (panickable? battlefield :cube-1) => false)


   (heavy-casualties? battlefield :cube-1) => true

   (provided
    (mu/unit-strength unit) => 11
    (panickable? battlefield :cube-1) => true)))
