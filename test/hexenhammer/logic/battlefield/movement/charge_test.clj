(ns hexenhammer.logic.battlefield.movement.charge-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement.core :as lbm]
            [hexenhammer.logic.battlefield.movement.charge :refer :all]))


(facts
 "list targets"

 (let [unit-1 {:unit/M 4}
       battlefield {:cube-1 unit-1
                    :cube-2 :terrain-1
                    :cube-3 :unit-2
                    :cube-4 :unit-3
                    :cube-5 :unit-4
                    :cube-6 :unit-5}]

   (list-targets battlefield :cube-1) => #{:cube-5}

   (provided
    (lbu/field-of-view battlefield :cube-1) => [:cube-2 :cube-3 :cube-4 :cube-5]
    (leu/unit? :terrain-1) => false

    (leu/unit? :unit-2) => true
    (leu/enemies? unit-1 :unit-2) => false

    (leu/unit? :unit-3) => true
    (leu/enemies? unit-1 :unit-3) => true
    (lc/distance :cube-1 :cube-4) => 4

    (leu/unit? :unit-4) => true
    (leu/enemies? unit-1 :unit-4) => true
    (lc/distance :cube-1 :cube-5) => 3)))


(facts
 "charge step"

 (charge-step (lc/->Pointer :cube-1 :n) :n)
 => [(lc/->Pointer :cube-1 :nw)
     (lc/->Pointer :cube-1 :ne)
     (lc/->Pointer :cube-2 :n)
     (lc/->Pointer :cube-2 :nw)
     (lc/->Pointer :cube-2 :ne)]

 (provided
  (lc/step :cube-1 :n)
  => :cube-2)


 (charge-step (lc/->Pointer :cube-1 :nw) :n)
 => [(lc/->Pointer :cube-1 :n)
     (lc/->Pointer :cube-2 :nw)
     (lc/->Pointer :cube-2 :n)]

 (provided
  (lc/step :cube-1 :nw)
  => :cube-2))


(facts
 "charge paths"

 (charge-paths :battlefield-1 :cube-1 #{})
 => {}

 (provided
  (lbu/unit-pointer :battlefield-1 :cube-1) => :pointer-1)


 (let [pointer-1 (lc/->Pointer :cube-1 :n)]

   (charge-paths :battlefield-1 :cube-1 #{:cube-2})
   => {}

   (provided
    (lbu/unit-pointer :battlefield-1 :cube-1) => pointer-1
    (lbu/move-unit :battlefield-1 :cube-1 pointer-1) => :battlefield-2
    (lbu/list-engaged :battlefield-2 :cube-1) => []
    (charge-step pointer-1 :n) => []))


 (let [pointer-1 (lc/->Pointer :cube-1 :n)]

   (charge-paths :battlefield-1 :cube-1 #{:cube-2})
   => {[pointer-1] #{:cube-2}}

   (provided
    (lbu/unit-pointer :battlefield-1 :cube-1) => pointer-1
    (lbu/move-unit :battlefield-1 :cube-1 pointer-1) => :battlefield-2
    (lbu/list-engaged :battlefield-2 :cube-1) => [:cube-2]
    (charge-step pointer-1 :n) => []))


 (let [pointer-1 (lc/->Pointer :cube-1 :n)]

   (charge-paths :battlefield-1 :cube-1 #{:cube-3})
   => {}

   (provided
    (lbu/unit-pointer :battlefield-1 :cube-1) => pointer-1
    (lbu/move-unit :battlefield-1 :cube-1 pointer-1) => :battlefield-2
    (lbu/list-engaged :battlefield-2 :cube-1) => [:cube-2]
    (charge-step pointer-1 :n) => []))


 (let [pointer-1 (lc/->Pointer :cube-1 :n)
       pointer-2 (lc/->Pointer :cube-2 :n)]

   (charge-paths :battlefield-1 :cube-1 #{:cube-3 :cube-4})
   => {[pointer-1] #{:cube-3}}

   (provided
    (lbu/unit-pointer :battlefield-1 :cube-1) => pointer-1
    (lbu/move-unit :battlefield-1 :cube-1 pointer-1) => :battlefield-2
    (lbu/list-engaged :battlefield-2 :cube-1) => [:cube-3]
    (charge-step pointer-1 :n) => [pointer-2]
    (lbm/valid-move? :battlefield-1 :cube-1 pointer-2) => true

    (lbu/move-unit :battlefield-1 :cube-1 pointer-2) => :battlefield-3
    (lbu/list-engaged :battlefield-3 :cube-2) => [:cube-3]
    (charge-step pointer-2 :n) => []))


 (let [pointer-1 (lc/->Pointer :cube-1 :n)]

   (charge-paths :battlefield-1 :cube-1 #{:cube-2})
   => {}

   (provided
    (lbu/unit-pointer :battlefield-1 :cube-1) => pointer-1
    (lbu/move-unit :battlefield-1 :cube-1 pointer-1) => :battlefield-2
    (lbu/list-engaged :battlefield-2 :cube-1) => [:cube-2 :cube-3]
    (charge-step pointer-1 :n) => [])))


(facts
 "charger?"

 (charger? :battlefield-1 :cube-1) => false

 (provided
  (lbm/movable? :battlefield-1 :cube-1) => false)


 (charger? :battlefield-1 :cube-1) => false

 (provided
  (lbm/movable? :battlefield-1 :cube-1) => true
  (list-targets :battlefield-1 :cube-1) => :list-targets
  (charge-paths :battlefield-1 :cube-1 :list-targets) => {})


 (charger? :battlefield-1 :cube-1) => true

 (provided
  (lbm/movable? :battlefield-1 :cube-1) => true
  (list-targets :battlefield-1 :cube-1) => :list-targets
  (charge-paths :battlefield-1 :cube-1 :list-targets) => {:path-1 :targets-1}))
