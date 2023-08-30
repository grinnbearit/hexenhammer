(ns hexenhammer.logic.movement-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.core :as l]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.model.entity :as me]
            [hexenhammer.logic.movement :refer :all]))


(facts
 "reform facings"

 (reform-facings :battlefield-1 :cube-1)
 => #{:n :sw :nw}

 (provided
  (l/move-unit :battlefield-1 :cube-1 (mc/->Pointer :cube-1 :n)) => :battlefield-2
  (l/battlefield-engaged? :battlefield-2 :cube-1) => false

  (l/move-unit :battlefield-1 :cube-1 (mc/->Pointer :cube-1 :ne)) => :battlefield-3
  (l/battlefield-engaged? :battlefield-3 :cube-1) => true

  (l/move-unit :battlefield-1 :cube-1 (mc/->Pointer :cube-1 :se)) => :battlefield-4
  (l/battlefield-engaged? :battlefield-4 :cube-1) => true

  (l/move-unit :battlefield-1 :cube-1 (mc/->Pointer :cube-1 :s)) => :battlefield-5
  (l/battlefield-engaged? :battlefield-5 :cube-1) => true

  (l/move-unit :battlefield-1 :cube-1 (mc/->Pointer :cube-1 :sw)) => :battlefield-6
  (l/battlefield-engaged? :battlefield-6 :cube-1) => false

  (l/move-unit :battlefield-1 :cube-1 (mc/->Pointer :cube-1 :nw)) => :battlefield-7
  (l/battlefield-engaged? :battlefield-7 :cube-1) => false))


(facts
 "show reform"

 (let [unit {:unit/player 1 :unit/facing :n}
       battlefield {:cube-1 unit}]

   (show-reform battlefield :cube-1)
   => {:cube-1 :swap-1}

   (provided
    (reform-facings battlefield :cube-1) => :facings-1

    (me/gen-mover :cube-1 1 :options :facings-1)
    => :mover-1

    (lt/swap :mover-1 unit)
    => :swap-1)))


(facts
 "forward step"

 (forward-step (mc/->Pointer :cube-1 :n))
 => [(mc/->Pointer :cube-1 :nw)
     (mc/->Pointer :cube-1 :ne)
     (mc/->Pointer :cube-2 :n)
     (mc/->Pointer :cube-2 :nw)
     (mc/->Pointer :cube-2 :ne)]

 (provided
  (mc/step :cube-1 :n)
  => :cube-2))


(facts
 "forward paths"

 (let [pointer-1 {:cube :cube-1}]

   (forward-paths :battlefield pointer-1 0)
   => [[pointer-1]]

   (provided
    (l/valid-end? :battlefield :cube-1 pointer-1) => true))


 (let [pointer-1 {:cube :cube-1}]

   (forward-paths :battlefield pointer-1 1)
   => [[pointer-1]]

   (provided
    (forward-step pointer-1) => []
    (l/valid-end? :battlefield :cube-1 pointer-1) => true))


 (let [pointer-1 {:cube :cube-1}]

   (forward-paths :battlefield pointer-1 0)
   => []

   (provided
    (l/valid-end? :battlefield :cube-1 pointer-1) => false))


 (let [pointer-1 {:cube :cube-1}]

   (forward-paths :battlefield pointer-1 1)
   => []

   (provided
    (forward-step pointer-1) => []
    (l/valid-end? :battlefield :cube-1 pointer-1) => false))


 (let [pointer-1 {:cube :cube-1 :facing :n}
       pointer-2 {:cube :cube-1 :facing :nw}
       pointer-3 {:cube :cube-1 :facing :ne}
       pointer-4 {:cube :cube-1 :facing :n}
       pointer-5 {:cube :cube-2 :facing :n}]

   (forward-paths :battlefield pointer-1 1)
   => [[pointer-1]
       [pointer-1 pointer-2]
       [pointer-1 pointer-3]]

   (provided

    (l/valid-end? :battlefield :cube-1 pointer-1) => true

    (forward-step pointer-1) => [pointer-2 pointer-3 pointer-4]
    (l/valid-move? :battlefield :cube-1 pointer-2) => true
    (l/valid-move? :battlefield :cube-1 pointer-3) => true
    (l/valid-move? :battlefield :cube-1 pointer-4) => false

    (l/valid-end? :battlefield :cube-1 pointer-2) => true

    (l/valid-end? :battlefield :cube-1 pointer-3) => true)))


(facts
 "reposition paths"

 (let [start (mc/->Pointer :cube-1 :n)

       pointer-1-ne (mc/->Pointer :cube-2-ne :n)
       pointer-1-se (mc/->Pointer :cube-2-se :n)
       pointer-1-s (mc/->Pointer :cube-2-s :n)
       pointer-1-sw (mc/->Pointer :cube-2-sw :n)
       pointer-1-nw (mc/->Pointer :cube-2-nw :n)

       pointer-2-ne (mc/->Pointer :cube-3-ne :n)
       pointer-2-se (mc/->Pointer :cube-3-se :n)
       pointer-2-s (mc/->Pointer :cube-3-s :n)
       pointer-2-sw (mc/->Pointer :cube-3-sw :n)]


   (reposition-paths :battlefield start 0)
   => [[start]]


   (reposition-paths :battlefield start 2)
   => (just #{[start]
              [start pointer-1-ne]
              [start pointer-1-se]
              [start pointer-1-s]
              [start pointer-1-ne pointer-2-ne]
              [start pointer-1-se pointer-2-se]
              [start pointer-1-s pointer-2-s]
              [start pointer-1-sw pointer-2-sw]})

   (provided
    (mc/step :cube-1 :ne) => :cube-2-ne
    (mc/step :cube-1 :se) => :cube-2-se
    (mc/step :cube-1 :s) => :cube-2-s
    (mc/step :cube-1 :sw) => :cube-2-sw
    (mc/step :cube-1 :nw) => :cube-2-nw

    (l/valid-move? :battlefield :cube-1 pointer-1-ne) => true
    (l/valid-move? :battlefield :cube-1 pointer-1-se) => true
    (l/valid-move? :battlefield :cube-1 pointer-1-s) => true
    (l/valid-move? :battlefield :cube-1 pointer-1-sw) => true
    (l/valid-move? :battlefield :cube-1 pointer-1-nw) => false

    (mc/step :cube-2-ne :ne) => :cube-3-ne
    (mc/step :cube-2-se :se) => :cube-3-se
    (mc/step :cube-2-s :s) => :cube-3-s
    (mc/step :cube-2-sw :sw) => :cube-3-sw

    (l/valid-move? :battlefield :cube-1 pointer-2-ne) => true
    (l/valid-move? :battlefield :cube-1 pointer-2-se) => true
    (l/valid-move? :battlefield :cube-1 pointer-2-s) => true
    (l/valid-move? :battlefield :cube-1 pointer-2-sw) => true

    (l/valid-end? :battlefield :cube-1 pointer-1-ne) => true
    (l/valid-end? :battlefield :cube-1 pointer-1-se) => true
    (l/valid-end? :battlefield :cube-1 pointer-1-s) => true
    (l/valid-end? :battlefield :cube-1 pointer-1-sw) => false

    (l/valid-end? :battlefield :cube-1 pointer-2-ne) => true
    (l/valid-end? :battlefield :cube-1 pointer-2-se) => true
    (l/valid-end? :battlefield :cube-1 pointer-2-s) => true
    (l/valid-end? :battlefield :cube-1 pointer-2-sw) => true)))


(facts
 "show-battlemap"

 (let [pointer-1 (mc/->Pointer :cube-1 :n)
       pointer-3 (mc/->Pointer :cube-1 :ne)
       pointer-4 (mc/->Pointer :cube-2 :ne)]

   (show-battlemap {:cube-1 :entity-1
                    :cube-2 :entity-2}
                   1
                   [[pointer-1]
                    [pointer-1 :pointer-2 pointer-3]
                    [pointer-1 :pointer-2 pointer-3 pointer-4]])
   => {:cube-1 :swap-1
       :cube-2 :swap-2}

   (provided
    (me/gen-mover :cube-1 1 :options #{:n :ne}) => :mover-1
    (lt/swap :mover-1 :entity-1) => :swap-1

    (me/gen-mover :cube-2 1 :options #{:ne}) => :mover-2
    (lt/swap :mover-2 :entity-2) => :swap-2)))


(facts
 "M -> hexes"

 (M->hexes 2) => 1
 (M->hexes 3) => 1
 (M->hexes 4) => 1
 (M->hexes 5) => 2)


(facts
 "compress path"

 (compress-path [{:cube :cube-1 :facing :n}])
 => []

 (compress-path [{:cube :cube-1 :facing :n}
                 {:cube :cube-1 :facing :ne}])
 => []

 (compress-path [{:cube :cube-1 :facing :n}
                 {:cube :cube-1 :facing :ne}
                 {:cube :cube-2 :facing :ne}])
 => [{:cube :cube-1 :facing :ne}]

 (compress-path [{:cube :cube-1 :facing :n}
                 {:cube :cube-1 :facing :ne}
                 {:cube :cube-2 :facing :ne}
                 {:cube :cube-2 :facing :n}])
 => [{:cube :cube-1 :facing :ne}])


(facts
 "path -> breadcrumbs"

 (let [pointer-1 (mc/->Pointer :cube-1 :n)
       pointer-2 (mc/->Pointer :cube-1 :ne)
       pointer-3 (mc/->Pointer :cube-2 :ne)]

   (path->breadcrumbs {:cube-2 :entity-2}
                      {:cube-1 {}}
                      1
                      [pointer-1 pointer-2 pointer-3])
   => {:cube-1 {:mover/highlighted :n
                :mover/state :past}
       :cube-2 :swap-2}

   (provided
    (compress-path [pointer-1 pointer-2 pointer-3])
    => [pointer-1 pointer-3]

    (me/gen-mover :cube-2 1 :highlighted :ne :state :past)
    => :mover-2

    (lt/swap :mover-2 :entity-2) => :swap-2)))


(facts
 "show breadcrumbs"

 (show-breadcrumbs :battlefield :battlemap 1 [[:pointer-1] [:pointer-1 :pointer-2]])
 => {:pointer-1 :breadcrumbs-1
     :pointer-2 :breadcrumbs-2}

 (provided
  (path->breadcrumbs :battlefield :battlemap 1 [:pointer-1]) => :breadcrumbs-1
  (path->breadcrumbs :battlefield :battlemap 1 [:pointer-1 :pointer-2]) => :breadcrumbs-2))


(facts
 "show moves"

 (let [battlefield {:cube-1 {:unit/facing :n
                             :unit/M 5
                             :unit/player 1}}

       path-fn (constantly :paths-1)]

   (show-moves battlefield :cube-1 :hexes path-fn)
   => {:battlemap :battlemap-1
       :breadcrumbs :breadcrumbs-1}

   (provided
    (show-battlemap battlefield 1 :paths-1) => :battlemap-1

    (show-breadcrumbs battlefield :battlemap-1 1 :paths-1) => :breadcrumbs-1)))


(facts
 "show forward"

 (let [battlefield {:cube-1 {:unit/M 8}}]

   (show-forward battlefield :cube-1)
   => :show-moves

   (provided
    (show-moves battlefield :cube-1 3 forward-paths)
    => :show-moves)))


(facts
 "show reposition"

 (let [battlefield {:cube-1 {:unit/M 8}}]

   (show-reposition battlefield :cube-1)
   => :show-moves

   (provided
    (show-moves battlefield :cube-1 1 reposition-paths)
    => :show-moves)))


(facts
 "show march"

 (let [battlefield {:cube-1 {:unit/M 8}}]

   (show-march battlefield :cube-1)
   => :show-moves

   (provided
    (show-moves battlefield :cube-1 5 forward-paths)
    => :show-moves)))


(facts
 "list threats"

 (list-threats {:cube-1 :unit-1} :cube-1)
 => []

 (provided
  (mc/neighbours-within :cube-1 3)
  => [])


 (list-threats {:cube-1 :unit-1} :cube-1)
 => []

 (provided
  (mc/neighbours-within :cube-1 3)
  => [:cube-2])


 (list-threats {:cube-1 :unit-1
                :cube-2 :terrain-1} :cube-1)
 => []

 (provided
  (mc/neighbours-within :cube-1 3)
  => [:cube-2]

  (le/unit? :terrain-1)
  => false)


 (list-threats {:cube-1 :unit-1
                :cube-2 :unit-2}
               :cube-1)
 => []

 (provided
  (mc/neighbours-within :cube-1 3)
  => [:cube-2]

  (le/unit? :unit-2)
  => true

  (l/enemies? :unit-1 :unit-2)
  => false)


 (list-threats {:cube-1 :unit-1
                :cube-2 :unit-2}
               :cube-1)
 => [:cube-2]

 (provided
  (mc/neighbours-within :cube-1 3)
  => [:cube-2]

  (le/unit? :unit-2)
  => true

  (l/enemies? :unit-1 :unit-2)
  => true))


(facts
 "show threats"

 (let [battlefield {:cube-2 {} :cube-3 {}}]

   (show-threats battlefield :cube-1)
   => {:cube-2 {:entity/state :marked}}

   (provided
    (list-threats battlefield :cube-1)
    => [:cube-2])))
