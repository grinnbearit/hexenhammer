(ns hexenhammer.logic.battlefield.event-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.event :as lev]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.event :refer :all]))


(facts
 "nearby friend annihilated"

 (let [battlefield {:cube-1 :terrain
                    :cube-2 :unit-1
                    :cube-3 :unit-2
                    :cube-4 :unit-3}]

   (nearby-friend-annihilated battlefield :cube-1 2)
   => [:panic]

   (provided
    (lc/neighbours-within :cube-1 2)
    => [:cube-2 :cube-3 :cube-4 :cube-5]

    (leu/unit? :terrain) => false
    (leu/unit? :unit-1) => true
    (leu/unit? :unit-2) => true
    (leu/unit? :unit-3) => true

    (leu/friendly? :unit-1 2) => false
    (leu/friendly? :unit-2 2) => true
    (leu/friendly? :unit-3 2) => true

    (lbu/panickable? battlefield :cube-3) => false
    (lbu/panickable? battlefield :cube-4) => true

    (leu/unit-key :unit-3) => :unit-key-3
    (lev/panic :unit-key-3)
    => :panic)))
