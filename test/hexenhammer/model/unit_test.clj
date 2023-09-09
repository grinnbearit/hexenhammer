(ns hexenhammer.model.unit-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.unit :refer :all]))


(facts
 "models"

 (models #:unit{:F 4 :ranks 3 :W 1 :damage 0}) => 12
 (models #:unit{:F 4 :ranks 3 :W 1 :damage 1}) => 11

 (models #:unit{:F 4 :ranks 3 :W 2 :damage 0}) => 12
 (models #:unit{:F 4 :ranks 3 :W 2 :damage 1}) => 12
 (models #:unit{:F 4 :ranks 3 :W 2 :damage 2}) => 11
 (models #:unit{:F 4 :ranks 3 :W 2 :damage 3}) => 11)


(facts
 "unit strength"

 (let [unit #:unit{:model-strength 1}]

   (unit-strength unit) => 10

   (provided
    (models unit) => 10))


 (let [unit #:unit{:model-strength 2}]

   (unit-strength unit) => 10

   (provided
    (models unit) => 5)))
