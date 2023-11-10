(ns hexenhammer.logic.battlefield.movement.march-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.event :as lev]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement.core :as lbm]
            [hexenhammer.logic.battlefield.movement.march :refer :all]))


(facts
 "path events"

 (let [battlefield-1 {:cube-1 :unit-1
                      :cube-2 :terrain-2
                      :cube-3 :terrain-3}
       battlefield-2 {:cube-1 :terrain-1
                      :cube-2 :terrain-2
                      :cube-3 :terrain-3}]

   (path-events battlefield-1 :cube-1 :path-1)
   => [:dangerous-1]

   (provided
    (lbu/unit-key battlefield-1 :cube-1) => :unit-key-1

    (lbu/remove-unit battlefield-1 :cube-1)
    => battlefield-2

    (lbm/compress-path :path-1)
    => [{:cube :cube-1}
        {:cube :cube-2}
        {:cube :cube-3}]

    (let/dangerous? :terrain-2) => true
    (lev/dangerous-terrain :cube-2 :unit-key-1) => :dangerous-1

    (let/dangerous? :terrain-3) => false)))


(facts
 "paths events"

 (paths-events :battlefield-1 :cube-1 [[:pointer-1]
                                       [:pointer-1 :pointer-2]])
 => {:pointer-1 [:event-1]
     :pointer-2 []}

 (provided
  (path-events :battlefield-1 :cube-1 [:pointer-1]) => [:event-1]
  (path-events :battlefield-1 :cube-1 [:pointer-1 :pointer-2]) => []))


(facts
 "list threats"

 (list-threats {:cube-1 :unit-1} :cube-1)
 => []

 (provided
  (lc/neighbours-within :cube-1 3)
  => [])


 (list-threats {:cube-1 :unit-1} :cube-1)
 => []

 (provided
  (lc/neighbours-within :cube-1 3)
  => [:cube-2])


 (list-threats {:cube-1 :unit-1
                :cube-2 :terrain-1} :cube-1)
 => []

 (provided
  (lc/neighbours-within :cube-1 3)
  => [:cube-2]

  (leu/unit? :terrain-1)
  => false)


 (list-threats {:cube-1 :unit-1
                :cube-2 :unit-2}
               :cube-1)
 => []

 (provided
  (lc/neighbours-within :cube-1 3)
  => [:cube-2]

  (leu/unit? :unit-2)
  => true

  (leu/enemies? :unit-1 :unit-2)
  => false)


 (list-threats {:cube-1 :unit-1
                :cube-2 :unit-2}
               :cube-1)
 => [:cube-2]

 (provided
  (lc/neighbours-within :cube-1 3)
  => [:cube-2]

  (leu/unit? :unit-2)
  => true

  (leu/enemies? :unit-1 :unit-2)
  => true))


(facts
 "march"

 (let [battlefield {:cube-1 {:unit/M 4}}]

   (march battlefield :cube-1)
   => {:cube->enders :cube->enders-1
       :pointer->cube->tweeners :pointer->cube->tweeners-1
       :pointer->events :pointer->events-1}

   (provided
    (lc/hexes 8) => :hexes
    (lbm/forward-paths battlefield :cube-1 :hexes) => :forward-paths
    (lbm/paths->enders battlefield :cube-1 :forward-paths) => :cube->enders-1
    (lbm/paths->tweeners battlefield :cube-1 :forward-paths) => :pointer->cube->tweeners-1
    (paths-events battlefield :cube-1 :forward-paths) => :pointer->events-1)))
