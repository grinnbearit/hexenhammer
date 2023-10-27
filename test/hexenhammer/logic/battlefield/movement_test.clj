(ns hexenhammer.logic.battlefield.movement-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.mover :as lem]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement :refer :all]))


(facts
 "valid move?"

 (let [battlefield-2 {:cube-1 :terrain-1}]

   (valid-move? :battlefield-1 :cube-1 (lc/->Pointer :cube-1 :n))
   => :passable?

   (provided
    (lbu/remove-unit :battlefield-1 :cube-1) => battlefield-2
    (let/passable? :terrain-1) => :passable?)))


(facts
 "valid end?"

 (let [pointer {:cube :cube-2}]

   (valid-end? :battlefield-1 :cube-1 {:cube :cube-2})
   => false

   (provided
    (lbu/move-unit :battlefield-1 :cube-1 pointer) => :battlefield-2

    (lbu/engaged? :battlefield-2 :cube-2) => true)


   (valid-end? :battlefield-1 :cube-1 {:cube :cube-2})
   => true

   (provided
    (lbu/move-unit :battlefield-1 :cube-1 pointer) => :battlefield-2

    (lbu/engaged? :battlefield-2 :cube-2) => false)))


(facts
 "reform paths"

 (let [start (lc/->Pointer :cube-1 :n)
       pointer-ne (lc/->Pointer :cube-1 :ne)
       pointer-se (lc/->Pointer :cube-1 :se)
       pointer-s (lc/->Pointer :cube-1 :s)
       pointer-sw (lc/->Pointer :cube-1 :sw)
       pointer-nw (lc/->Pointer :cube-1 :nw)]

   (reform-paths :battlefield-1 :cube-1)
   => [[start pointer-se]
       [start pointer-sw]]

   (provided
    (lbu/unit-pointer :battlefield-1 :cube-1) => start
    (valid-end? :battlefield-1 :cube-1 pointer-ne) => false
    (valid-end? :battlefield-1 :cube-1 pointer-se) => true
    (valid-end? :battlefield-1 :cube-1 pointer-s) => false
    (valid-end? :battlefield-1 :cube-1 pointer-sw) => true
    (valid-end? :battlefield-1 :cube-1 pointer-nw) => false)))


(facts
 "forward step"

 (forward-step (lc/->Pointer :cube-1 :n))
 => [(lc/->Pointer :cube-1 :nw)
     (lc/->Pointer :cube-1 :ne)
     (lc/->Pointer :cube-2 :n)
     (lc/->Pointer :cube-2 :nw)
     (lc/->Pointer :cube-2 :ne)]

 (provided
  (lc/step :cube-1 :n) => :cube-2))


(facts
 "forward paths"

 (forward-paths :battlefield-1 :cube-1 0)
 => []

 (provided
  (lbu/unit-pointer :battlefield-1 :cube-1) => :pointer-1
  (valid-end? :battlefield-1 :cube-1 :pointer-1) => true)


 (forward-paths :battlefield-1 :cube-1 1)
 => []

 (provided
  (lbu/unit-pointer :battlefield-1 :cube-1) => :pointer-1
  (forward-step :pointer-1) => []
  (valid-end? :battlefield-1 :cube-1 :pointer-1) => true)


 (forward-paths :battlefield-1 :cube-1 0)
 => []

 (provided
  (lbu/unit-pointer :battlefield-1 :cube-1) => :pointer-1
  (valid-end? :battlefield-1 :cube-1 :pointer-1) => false)


 (forward-paths :battlefield-1 :cube-1 1)
 => []

 (provided
  (lbu/unit-pointer :battlefield-1 :cube-1) => :pointer-1
  (forward-step :pointer-1) => []
  (valid-end? :battlefield-1 :cube-1 :pointer-1) => false)


 (forward-paths :battlefield-1 :cube-1 1)
 => [[:pointer-1 :pointer-2]
     [:pointer-1 :pointer-3]]

 (provided
  (lbu/unit-pointer :battlefield-1 :cube-1) => :pointer-1
  (valid-end? :battlefield-1 :cube-1 :pointer-1) => true

  (forward-step :pointer-1) => [:pointer-2 :pointer-3 :pointer-4]
  (valid-move? :battlefield-1 :cube-1 :pointer-2) => true
  (valid-move? :battlefield-1 :cube-1 :pointer-3) => true
  (valid-move? :battlefield-1 :cube-1 :pointer-4) => false

  (valid-end? :battlefield-1 :cube-1 :pointer-2) => true
  (valid-end? :battlefield-1 :cube-1 :pointer-3) => true))


(facts
 "paths -> enders"

 (let [battlefield {:cube-1 {:unit/player 1}}
       pointer-1 (lc/->Pointer :cube-1 :n)
       pointer-3 (lc/->Pointer :cube-1 :ne)
       pointer-4 (lc/->Pointer :cube-2 :ne)]

   (paths->enders battlefield
                  :cube-1
                  [[pointer-1]
                   [pointer-1 :pointer-2 pointer-3]
                   [pointer-1 :pointer-2 pointer-3 pointer-4]])
   => {:cube-1 :place-1
       :cube-2 :place-2}

   (provided
    (lbu/remove-unit battlefield :cube-1)
    => {:cube-1 :terrain-1
        :cube-2 :terrain-2}

    (lem/gen-mover 1 :options #{:n :ne}) => :mover-1
    (let/place :terrain-1 :mover-1) => :place-1

    (lem/gen-mover 1 :options #{:ne}) => :mover-2
    (let/place :terrain-2 :mover-2) => :place-2)))


(facts
 "reform"

 (reform :battlefield-1 :cube-1)
 => {:cube->enders :cube->enders-1}

 (provided
  (reform-paths :battlefield-1 :cube-1) => :paths-1
  (paths->enders :battlefield-1 :cube-1 :paths-1) => :cube->enders-1))


(facts
 "forward"

 (let [battlefield {:cube-1 {:unit/M :M-1}}]

   (forward battlefield :cube-1)
   => {:cube->enders :cube->enders-1}

   (provided
    (lc/hexes :M-1) => :hexes
    (forward-paths battlefield :cube-1 :hexes) => :forward-paths
    (paths->enders battlefield :cube-1 :forward-paths) => :cube->enders-1)))
