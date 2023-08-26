(ns hexenhammer.logic.movement-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.core :as lc]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.model.entity :as me]
            [hexenhammer.logic.movement :refer :all]))


(facts
 "pointer -> shadow"

 (pointer->shadow 1 (mc/->Pointer :cube-1 :facing-1))
 => :shadow-1

 (provided
  (me/gen-shadow :cube-1 1 :facing-1) => :shadow-1))


(facts
 "reform facings"

 (reform-facings {:cube-1 {:unit/player 1 :unit/facing :n}} :cube-1)
 => #{:n :sw :nw}

 (provided
  (pointer->shadow 1 (mc/->Pointer :cube-1 :n)) => :shadow-n
  (lc/battlefield-engaged? {:cube-1 :shadow-n} :cube-1) => false

  (pointer->shadow 1 (mc/->Pointer :cube-1 :ne)) => :shadow-ne
  (lc/battlefield-engaged? {:cube-1 :shadow-ne} :cube-1) => true

  (pointer->shadow 1 (mc/->Pointer :cube-1 :se)) => :shadow-se
  (lc/battlefield-engaged? {:cube-1 :shadow-se} :cube-1) => true

  (pointer->shadow 1 (mc/->Pointer :cube-1 :s)) => :shadow-s
  (lc/battlefield-engaged? {:cube-1 :shadow-s} :cube-1) => true

  (pointer->shadow 1 (mc/->Pointer :cube-1 :sw)) => :shadow-sw
  (lc/battlefield-engaged? {:cube-1 :shadow-sw} :cube-1) => false

  (pointer->shadow 1 (mc/->Pointer :cube-1 :nw)) => :shadow-nw
  (lc/battlefield-engaged? {:cube-1 :shadow-nw} :cube-1) => false))


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
 "valid pointer?"

 (valid-pointer? {} (mc/->Pointer :cube-1 :facing-1))
 => false

 (valid-pointer? {:cube-1 :unit-1} (mc/->Pointer :cube-1 :facing-1))
 => false

 (provided
  (lt/passable? :unit-1) => false)

 (valid-pointer? {:cube-1 :terrain-1} (mc/->Pointer :cube-1 :facing-1))
 => true

 (provided
  (lt/passable? :terrain-1) => true))


(facts
 "forward paths"

 (forward-paths :battlefield :pointer-1 0)
 => [[:pointer-1]]


 (forward-paths :battlefield :pointer-1 1)
 => [[:pointer-1]]

 (provided
  (forward-step :pointer-1) => [])


 (forward-paths :battlefield :pointer-1 1)
 => [[:pointer-1]]

 (provided
  (forward-step :pointer-1) => [:pointer-2]
  (valid-pointer? :battlefield :pointer-2) => false)


 (forward-paths :battlefield :pointer-1 1)
 => [[:pointer-1 :pointer-2]]

 (provided
  (forward-step :pointer-1) => [:pointer-2]
  (valid-pointer? :battlefield :pointer-2) => true)


 (forward-paths :battlefield :pointer-1 2)
 => [[:pointer-1 :pointer-2]]

 (provided
  (forward-step :pointer-1) => [:pointer-2]
  (valid-pointer? :battlefield :pointer-2) => true
  (forward-step :pointer-2) => [:pointer-1]
  (valid-pointer? :battlefield :pointer-1) => true))


(facts
 "reposition paths"

 (let [pointer (mc/->Pointer :cube-1 :n)
       pointer-ne (mc/->Pointer :cube-1-ne :n)
       pointer-se (mc/->Pointer :cube-1-se :n)
       pointer-s (mc/->Pointer :cube-1-s :n)
       pointer-sw (mc/->Pointer :cube-1-sw :n)
       pointer-nw (mc/->Pointer :cube-1-nw :n)]

   (reposition-paths :battlefield pointer 1)
   => (just #{[pointer pointer-ne]
              [pointer pointer-se]
              [pointer pointer-s]
              [pointer pointer-sw]
              [pointer pointer-nw]})

   (provided
    (mc/step :cube-1 :ne) => :cube-1-ne
    (mc/step :cube-1 :se) => :cube-1-se
    (mc/step :cube-1 :s) => :cube-1-s
    (mc/step :cube-1 :sw) => :cube-1-sw
    (mc/step :cube-1 :nw) => :cube-1-nw

    (valid-pointer? :battlefield pointer-ne) => true
    (valid-pointer? :battlefield pointer-se) => true
    (valid-pointer? :battlefield pointer-s) => true
    (valid-pointer? :battlefield pointer-sw) => true
    (valid-pointer? :battlefield pointer-nw) => true)))


(facts
 "paths -> path-map"

 (paths->path-map [[:pointer-1]
                   [:pointer-1 :pointer-2]
                   [:pointer-1 :pointer-3 :pointer-4]])
 => {:pointer-1 [:pointer-1]
     :pointer-2 [:pointer-1 :pointer-2]
     :pointer-3 [:pointer-1 :pointer-3]
     :pointer-4 [:pointer-1 :pointer-3 :pointer-4]})


(facts
 "path-map -> battlemap"

 (let [pointer-1 (mc/->Pointer :cube-1 :n)
       pointer-2 (mc/->Pointer :cube-1 :ne)
       pointer-3 (mc/->Pointer :cube-2 :ne)
       pointer-4 (mc/->Pointer :cube-2 :n)]

   (path-map->battlemap {:cube-1 :entity-1
                         :cube-2 :entity-2}
                        1
                        {pointer-1 [pointer-1]
                         pointer-4 [pointer-1 pointer-2 pointer-3 pointer-4]})
   => {:cube-1 :swap-1
       :cube-2 :swap-2}

   (provided
    (pointer->shadow 1 pointer-1) => :shadow-1
    (lc/battlefield-engaged? {:cube-1 :shadow-1 :cube-2 :entity-2} :cube-1) => false

    (pointer->shadow 1 pointer-2) => :shadow-2
    (lc/battlefield-engaged? {:cube-1 :shadow-2 :cube-2 :entity-2} :cube-1) => false

    (pointer->shadow 1 pointer-3) => :shadow-3
    (lc/battlefield-engaged? {:cube-1 :entity-1 :cube-2 :shadow-3} :cube-2) => true

    (pointer->shadow 1 pointer-4) => :shadow-4
    (lc/battlefield-engaged? {:cube-1 :entity-1 :cube-2 :shadow-4} :cube-2) => false

    (me/gen-mover :cube-1 1 :options #{:n :ne}) => :mover-1
    (lt/swap :mover-1 :entity-1) => :swap-1

    (me/gen-mover :cube-2 1 :options #{:n}) => :mover-2
    (lt/swap :mover-2 :entity-2) => :swap-2)))


(facts
 "M -> hexes"

 (M->hexes 2) => 1
 (M->hexes 3) => 1
 (M->hexes 4) => 1
 (M->hexes 5) => 2)


(facts
 "remove unit"

 (remove-unit {:cube-1 :unit-1} :cube-1)
 => {:cube-1 :terrain-1}

 (provided
  (lt/pickup :unit-1) => :terrain-1))


(facts
 "show moves"

 (let [battlefield {:cube-1 {:unit/facing :n
                             :unit/M 5
                             :unit/player 1}}

       path-fn (constantly :paths-1)]

   (show-moves battlefield :cube-1 :hexes path-fn)
   => {:battlemap :battlemap-1
       :path-map :path-map-1}

   (provided
    (remove-unit battlefield :cube-1) => :new-battlefield

    (paths->path-map :paths-1) => :path-map-1

    (path-map->battlemap :new-battlefield 1 :path-map-1)
    => :battlemap-1)))


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
 "show breadcrumbs"

 (let [pointer-1 (mc/->Pointer :cube-1 :n)
       pointer-2 (mc/->Pointer :cube-1 :ne)
       pointer-3 (mc/->Pointer :cube-2 :ne)]

   (show-breadcrumbs {:cube-2 :entity-2}
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

  (lc/enemies? :unit-1 :unit-2)
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

  (lc/enemies? :unit-1 :unit-2)
  => true))


(facts
 "show threats"

 (let [battlefield {:cube-2 {} :cube-3 {}}]

   (show-threats battlefield :cube-1)
   => {:cube-2 {:entity/state :marked}}

   (provided
    (list-threats battlefield :cube-1)
    => [:cube-2])))
