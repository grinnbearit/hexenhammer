(ns hexenhammer.logic.battlefield.movement.flee-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.mover :as lem]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement.core :as lbm]
            [hexenhammer.logic.battlefield.movement.flee :refer :all]))


(facts
 "flee direction"

 (let [pointer (lc/->Pointer :cube-1 :n)]

   (flee-direction pointer :cube-1) => pointer


   (flee-direction pointer :cube-2) => (lc/->Pointer :cube-1 :s)

   (provided
    (lc/direction :cube-2 :cube-1) => :s)))


(facts
 "flee step"

 (flee-step (lc/->Pointer :cube-1 :n))
 => (lc/->Pointer :cube-2 :n)

 (provided
  (lc/step :cube-1 :n) => :cube-2))


(facts
 "flee path"

 (flee-path :battlefield-1 :cube-1 :cube-1 0)
 => {:path [:pointer-1] :edge? false}

 (provided
  (lbu/unit-pointer :battlefield-1 :cube-1) => :pointer-1
  (flee-direction :pointer-1 :cube-1) => :pointer-1
  (lbm/valid-move? :battlefield-1 :cube-1 :pointer-1) => true
  (lbm/valid-end? :battlefield-1 :cube-1 :pointer-1) => true)


 (flee-path :battlefield-1 :cube-1 :cube-2 0)
 => {:path [:pointer-1 :pointer-2] :edge? false}

 (provided
  (lbu/unit-pointer :battlefield-1 :cube-1) => :pointer-1
  (flee-direction :pointer-1 :cube-2) => :pointer-2
  (lbm/valid-move? :battlefield-1 :cube-1 :pointer-2) => true
  (lbm/valid-end? :battlefield-1 :cube-1 :pointer-2) => true)


 (let [battlefield {:cube-2 :entity-1}
       pointer-2 (lc/->Pointer :cube-2 :n)]

   (flee-path battlefield :cube-1 :cube-1 1)
   => {:path [:pointer-1 pointer-2] :edge? false}

   (provided
    (lbu/unit-pointer battlefield :cube-1) => :pointer-1
    (flee-direction :pointer-1 :cube-1) => :pointer-1
    (flee-step :pointer-1) => pointer-2
    (lbm/valid-move? battlefield :cube-1 pointer-2) => true
    (lbm/valid-end? battlefield :cube-1 pointer-2) => true))


 (let [battlefield {:cube-2 :entity-1}
       pointer-2 (lc/->Pointer :cube-2 :n)]

   (flee-path battlefield :cube-1 :cube-1 0)
   => {:path [:pointer-1 pointer-2] :edge? false}

   (provided
    (lbu/unit-pointer battlefield :cube-1) => :pointer-1
    (flee-direction :pointer-1 :cube-1) => :pointer-1
    (lbm/valid-move? battlefield :cube-1 :pointer-1) => false
    (flee-step :pointer-1) => pointer-2
    (lbm/valid-move? battlefield :cube-1 pointer-2) => true
    (lbm/valid-end? battlefield :cube-1 pointer-2) => true))


 (let [battlefield {:cube-2 :entity-1}
       pointer-2 (lc/->Pointer :cube-2 :n)]

   (flee-path battlefield :cube-1 :cube-1 0)
   => {:path [:pointer-1 pointer-2] :edge? false}

   (provided
    (lbu/unit-pointer battlefield :cube-1) => :pointer-1
    (flee-direction :pointer-1 :cube-1) => :pointer-1
    (lbm/valid-move? battlefield :cube-1 :pointer-1) => true
    (lbm/valid-end? battlefield :cube-1 :pointer-1) => false
    (flee-step :pointer-1) => pointer-2
    (lbm/valid-move? battlefield :cube-1 pointer-2) => true
    (lbm/valid-end? battlefield :cube-1 pointer-2) => true))


 (let [battlefield {}
       pointer-2 (lc/->Pointer :cube-2 :n)]

   (flee-path battlefield :cube-1 :cube-1 1)
   => {:path [:pointer-1] :edge? true}

   (provided
    (lbu/unit-pointer battlefield :cube-1) => :pointer-1
    (flee-direction :pointer-1 :cube-1) => :pointer-1
    (flee-step :pointer-1) => pointer-2)))


(facts
 "compress path"

 (compress-path [:pointer-1])
 => [:pointer-1]


 (let [pointer-1 (lc/->Pointer :cube-1 :n)
       pointer-2 (lc/->Pointer :cube-2 :n)]
   (compress-path [pointer-1 pointer-2])
   => [pointer-1 pointer-2])


 (let [pointer-1 (lc/->Pointer :cube-1 :n)
       pointer-2 (lc/->Pointer :cube-1 :s)]
   (compress-path [pointer-1 pointer-2])
   => [pointer-2]))


(facts
 "path -> tweeners"

 (let [battlefield {:cube-1 {:unit/player 1}}]

   (path->tweeners battlefield :cube-1 :path-1 false)
   => {:cube-1 :place}

   (provided
    (lbu/remove-unit battlefield :cube-1)
    => {:cube-1 :terrain-1
        :cube-2 :terrain-2}

    (compress-path :path-1)
    => [{:cube :cube-1 :facing :n}
        {:cube :cube-2 :facing :n}]

    (let/passable? :terrain-1) => true

    (lem/gen-mover 1 :highlighted :n :presentation :past)
    => :mover-1

    (let/place :terrain-1 :mover-1)
    => :place))


 (let [battlefield {:cube-1 {:unit/player 1}}]

   (path->tweeners battlefield :cube-1 :path-1 true)
   => {:cube-1 :place}

   (provided
    (lbu/remove-unit battlefield :cube-1)
    => {:cube-1 :terrain-1
        :cube-2 :terrain-2}

    (compress-path :path-1)
    => [{:cube :cube-1 :facing :n}
        {:cube :cube-2 :facing :n}]

    (let/passable? :terrain-1) => true
    (let/passable? :terrain-2) => false

    (lem/gen-mover 1 :highlighted :n :presentation :past)
    => :mover-1

    (let/place :terrain-1 :mover-1)
    => :place)))


(facts
 "flee"

 (let [path [:pointer-1 :pointer-2]]

   (flee :battlefield-1 :cube-1 :cube-2 3)
   => {:end :pointer-2
       :cube->tweeners :cube->tweeners-1
       :edge? false}

   (provided
    (lc/hexes 3) => 1

    (flee-path :battlefield-1 :cube-1 :cube-2 1)
    => {:path path :edge? false}

    (path->tweeners :battlefield-1 :cube-1 path false)
    => :cube->tweeners-1)))
