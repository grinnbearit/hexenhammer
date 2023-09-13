(ns hexenhammer.logic.terrain-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.unit :as mu]
            [hexenhammer.logic.unit :refer :all]))


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

 (phase-reset {:cube-1 {:entity/name "unit-1"}
               :cube-2 {:entity/name "unit-2"}
               :cube-3 {:entity/name "terrain"}}
              [:cube-1 :cube-2])
 => {:cube-1 {:entity/name "unit-1"
              :unit/phase-strength 10}
     :cube-2 {:entity/name "unit-2"
              :unit/phase-strength 8}
     :cube-3 {:entity/name "terrain"}}

 (provided
  (mu/unit-strength {:entity/name "unit-1"}) => 10
  (mu/unit-strength {:entity/name "unit-2"}) => 8))
