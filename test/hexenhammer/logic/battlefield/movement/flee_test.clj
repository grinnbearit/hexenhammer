(ns hexenhammer.logic.battlefield.movement.flee-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.mover :as lem]
            [hexenhammer.logic.entity.event :as lev]
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
 "path events"

 (let [battlefield-1 {:cube-1 :unit-1}
       battlefield-2 {:cube-2 :terrain-1
                      :cube-3 :terrain-2
                      :cube-4 :terrain-3
                      :cube-5 :unit-2
                      :cube-6 :unit-3
                      :cube-7 :unit-4
                      :cube-8 :unit-5}]

   (path-events battlefield-1 :cube-1 :path-1)
   => [:dangerous-terrain-1
       :dangerous-terrain-2
       :opportunity-attack
       :panic]

   (provided
    (leu/unit-key :unit-1) => :unit-key-1
    (lbu/remove-unit battlefield-1 :cube-1) => battlefield-2

    (compress-path :path-1)
    => [{:cube :cube-1}
        {:cube :cube-2}
        {:cube :cube-3}
        {:cube :cube-4}
        {:cube :cube-5}
        {:cube :cube-6}
        {:cube :cube-7}
        {:cube :cube-8}]

    (let/terrain? :terrain-1) => true
    (let/dangerous? :terrain-1) => false
    (let/impassable? :terrain-1) => false

    (let/terrain? :terrain-2) => true
    (let/dangerous? :terrain-2) => true
    (lev/dangerous-terrain :cube-3 :unit-key-1) => :dangerous-terrain-1

    (let/terrain? :terrain-3) => true
    (let/dangerous? :terrain-3) => false
    (let/impassable? :terrain-3) => true
    (lev/dangerous-terrain :cube-4 :unit-key-1) => :dangerous-terrain-2

    (let/terrain? :unit-2) => false
    (leu/enemies? :unit-1 :unit-2) => true
    (leu/fleeing? :unit-2) => true

    (let/terrain? :unit-3) => false
    (leu/enemies? :unit-1 :unit-3) => true
    (leu/fleeing? :unit-3) => false
    (leu/unit-strength :unit-3) => 10
    (lev/opportunity-attack :cube-6 :unit-key-1 10) => :opportunity-attack

    (let/terrain? :unit-4) => false
    (leu/enemies? :unit-1 :unit-4) => false
    (leu/unit-strength :unit-1) => 8
    (lbu/panickable? battlefield-2 :cube-7) => false

    (let/terrain? :unit-5) => false
    (leu/enemies? :unit-1 :unit-5) => false
    (lbu/panickable? battlefield-2 :cube-8) => true
    (leu/unit-key :unit-5) => :unit-key-5
    (lev/panic :unit-key-5) => :panic))


 (let [battlefield-1 {:cube-1 :unit-1}
       battlefield-2 {:cube-2 :unit-2}]

   (path-events battlefield-1 :cube-1 :path-1)
   => []

   (provided
    (leu/unit-key :unit-1) => :unit-key-1
    (lbu/remove-unit battlefield-1 :cube-1) => battlefield-2

    (compress-path :path-1)
    => [{:cube :cube-1}
        {:cube :cube-2}]

    (let/terrain? :unit-2) => false
    (leu/enemies? :unit-1 :unit-2) => false
    (leu/unit-strength :unit-1) => 7)))


(facts
 "flee"

 (let [path [:pointer-1 :pointer-2]]

   (flee :battlefield-1 :cube-1 :cube-2 3)
   => {:end :pointer-2
       :cube->tweeners :cube->tweeners-1
       :edge? false
       :events :events-1}

   (provided
    (lc/hexes 3) => 1

    (flee-path :battlefield-1 :cube-1 :cube-2 1)
    => {:path path :edge? false}

    (path->tweeners :battlefield-1 :cube-1 path false)
    => :cube->tweeners-1

    (path-events :battlefield-1 :cube-1 path)
    => :events-1)))
