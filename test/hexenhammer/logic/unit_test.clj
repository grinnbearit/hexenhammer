(ns hexenhammer.logic.terrain-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.unit :as mu]
            [hexenhammer.model.logic.unit :refer :all]))


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
