(ns hexenhammer.logic.battlefield.movement.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.mover :as lem]
            [hexenhammer.logic.entity.event :as lev]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement.core :refer :all]))


(facts
 "movable?"

 (let [battlefield {:cube-1 :unit-1}]

   (movable? battlefield :cube-1)
   => false

   (provided
    (lbu/engaged? battlefield :cube-1) => true)


   (movable? battlefield :cube-1)
   => false

   (provided
    (lbu/engaged? battlefield :cube-1) => false
    (leu/fleeing? :unit-1) => true)


   (movable? battlefield :cube-1)
   => true

   (provided
    (lbu/engaged? battlefield :cube-1) => false
    (leu/fleeing? :unit-1) => false)))


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
 "reposition paths"

 (let [start (lc/->Pointer :cube-1 :n)

       pointer-1-ne (lc/->Pointer :cube-2-ne :n)
       pointer-1-se (lc/->Pointer :cube-2-se :n)
       pointer-1-s (lc/->Pointer :cube-2-s :n)
       pointer-1-sw (lc/->Pointer :cube-2-sw :n)
       pointer-1-nw (lc/->Pointer :cube-2-nw :n)

       pointer-2-ne (lc/->Pointer :cube-3-ne :n)
       pointer-2-se (lc/->Pointer :cube-3-se :n)
       pointer-2-s (lc/->Pointer :cube-3-s :n)
       pointer-2-sw (lc/->Pointer :cube-3-sw :n)]


   (reposition-paths :battlefield :cube-1 0)
   => []

   (provided
    (lbu/unit-pointer :battlefield :cube-1) => start)


   (reposition-paths :battlefield :cube-1 2)
   => (just #{[start pointer-1-ne]
              [start pointer-1-se]
              [start pointer-1-s]
              [start pointer-1-ne pointer-2-ne]
              [start pointer-1-se pointer-2-se]
              [start pointer-1-s pointer-2-s]
              [start pointer-1-sw pointer-2-sw]})

   (provided
    (lbu/unit-pointer :battlefield :cube-1) => start

    (lc/step :cube-1 :ne) => :cube-2-ne
    (lc/step :cube-1 :se) => :cube-2-se
    (lc/step :cube-1 :s) => :cube-2-s
    (lc/step :cube-1 :sw) => :cube-2-sw
    (lc/step :cube-1 :nw) => :cube-2-nw

    (valid-move? :battlefield :cube-1 pointer-1-ne) => true
    (valid-move? :battlefield :cube-1 pointer-1-se) => true
    (valid-move? :battlefield :cube-1 pointer-1-s) => true
    (valid-move? :battlefield :cube-1 pointer-1-sw) => true
    (valid-move? :battlefield :cube-1 pointer-1-nw) => false

    (lc/step :cube-2-ne :ne) => :cube-3-ne
    (lc/step :cube-2-se :se) => :cube-3-se
    (lc/step :cube-2-s :s) => :cube-3-s
    (lc/step :cube-2-sw :sw) => :cube-3-sw

    (valid-move? :battlefield :cube-1 pointer-2-ne) => true
    (valid-move? :battlefield :cube-1 pointer-2-se) => true
    (valid-move? :battlefield :cube-1 pointer-2-s) => true
    (valid-move? :battlefield :cube-1 pointer-2-sw) => true

    (valid-end? :battlefield :cube-1 pointer-1-ne) => true
    (valid-end? :battlefield :cube-1 pointer-1-se) => true
    (valid-end? :battlefield :cube-1 pointer-1-s) => true
    (valid-end? :battlefield :cube-1 pointer-1-sw) => false

    (valid-end? :battlefield :cube-1 pointer-2-ne) => true
    (valid-end? :battlefield :cube-1 pointer-2-se) => true
    (valid-end? :battlefield :cube-1 pointer-2-s) => true
    (valid-end? :battlefield :cube-1 pointer-2-sw) => true)))


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
 "compress path"

 (compress-path [{:cube :cube-1 :facing :n}])
 => [{:cube :cube-1 :facing :n}]

 (compress-path [{:cube :cube-1 :facing :n}
                 {:cube :cube-1 :facing :ne}])
 => [{:cube :cube-1 :facing :ne}]

 (compress-path [{:cube :cube-1 :facing :n}
                 {:cube :cube-1 :facing :ne}
                 {:cube :cube-2 :facing :ne}])
 => [{:cube :cube-1 :facing :ne}
     {:cube :cube-2 :facing :ne}]

 (compress-path [{:cube :cube-1 :facing :n}
                 {:cube :cube-1 :facing :ne}
                 {:cube :cube-2 :facing :ne}
                 {:cube :cube-2 :facing :n}])
 => [{:cube :cube-1 :facing :ne}
     {:cube :cube-2 :facing :n}])


(facts
 "path -> tweeners"

 (let [battlefield {:cube-1 {:unit/player 1}}
       cube->enders {:cube-2 {}
                     :cube-3 {}}]

   (path->tweeners battlefield :cube-1 cube->enders :path-1)
   => {:cube-1 :place
       :cube-2 {:mover/highlighted :n
                :mover/presentation :past}
       :cube-3 {:mover/selected :n
                :mover/presentation :present}}

   (provided

    (lbu/remove-unit battlefield :cube-1)
    => {:cube-1 :terrain-1}

    (compress-path :path-1)
    => [{:cube :cube-1 :facing :n}
        {:cube :cube-2 :facing :n}
        {:cube :cube-3 :facing :n}]

    (lem/gen-mover 1 :highlighted :n :presentation :past)
    => :mover-1

    (let/place :terrain-1 :mover-1)
    => :place)))


(facts
 "paths -> tweeners"

 (paths->tweeners :battlefield-1 :cube-1 :cube->enders-1 [[:pointer-1] [:pointer-1 :pointer-2]])
 => {:pointer-1 :tweeners-1
     :pointer-2 :tweeners-2}

 (provided
  (path->tweeners :battlefield-1 :cube-1 :cube->enders-1 [:pointer-1]) => :tweeners-1
  (path->tweeners :battlefield-1 :cube-1 :cube->enders-1 [:pointer-1 :pointer-2]) => :tweeners-2))


(facts
 "reform"

 (reform :battlefield-1 :cube-1)
 => {:cube->enders :cube->enders-1
     :pointer->cube->tweeners :pointer->cube->tweeners-1}

 (provided
  (reform-paths :battlefield-1 :cube-1)
  => :paths-1

  (paths->enders :battlefield-1 :cube-1 :paths-1)
  => :cube->enders-1

  (paths->tweeners :battlefield-1 :cube-1 :cube->enders-1 :paths-1)
  => :pointer->cube->tweeners-1))


(facts
 "forward"

 (let [battlefield {:cube-1 {:unit/M 4}}]

   (forward battlefield :cube-1)
   => {:cube->enders :cube->enders-1
       :pointer->cube->tweeners :pointer->cube->tweeners-1}

   (provided
    (lc/hexes 4) => :hexes

    (forward-paths battlefield :cube-1 :hexes)
    => :forward-paths

    (paths->enders battlefield :cube-1 :forward-paths)
    => :cube->enders-1

    (paths->tweeners battlefield :cube-1 :cube->enders-1 :forward-paths)
    => :pointer->cube->tweeners-1)))


(facts
 "reposition"

 (let [battlefield {:cube-1 {:unit/M 4}}]

   (reposition battlefield :cube-1)
   => {:cube->enders :cube->enders-1
       :pointer->cube->tweeners :pointer->cube->tweeners-1}

   (provided
    (lc/hexes 2) => :hexes

    (reposition-paths battlefield :cube-1 :hexes)
    => :reposition-paths

    (paths->enders battlefield :cube-1 :reposition-paths)
    => :cube->enders-1

    (paths->tweeners battlefield :cube-1 :cube->enders-1 :reposition-paths)
    => :pointer->cube->tweeners-1)))
