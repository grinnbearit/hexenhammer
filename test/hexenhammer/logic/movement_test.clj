(ns hexenhammer.logic.movement-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.core :as l]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.model.entity :as me]
            [hexenhammer.model.event :as mv]
            [hexenhammer.logic.movement :refer :all]))


(facts
 "valid move?"

 (let [pointer {:cube :cube-2}]

   (valid-move? :battlefield-1 :cube-1 {:cube :cube-2})
   => false

   (provided
    (l/remove-unit :battlefield-1 :cube-1) => {:cube-2 :terrain}

    (lt/passable? :terrain) => false)


   (valid-move? :battlefield-1 :cube-1 {:cube :cube-2})
   => true

   (provided
    (l/remove-unit :battlefield-1 :cube-1) => {:cube-2 :terrain}

    (lt/passable? :terrain) => true)))


(facts
 "valid end?"

 (let [pointer {:cube :cube-2}]

   (valid-end? :battlefield-1 :cube-1 {:cube :cube-2})
   => false

   (provided
    (l/move-unit :battlefield-1 :cube-1 pointer) => :battlefield-2

    (l/battlefield-engaged? :battlefield-2 :cube-2) => true)


   (valid-end? :battlefield-1 :cube-1 {:cube :cube-2})
   => true

   (provided
    (l/move-unit :battlefield-1 :cube-1 pointer) => :battlefield-2

    (l/battlefield-engaged? :battlefield-2 :cube-2) => false)))


(facts
 "reform facings"

 (reform-facings :battlefield-1 :cube-1)
 => #{:n :sw :nw}

 (provided
  (valid-end? :battlefield-1 :cube-1 (mc/->Pointer :cube-1 :n)) => true
  (valid-end? :battlefield-1 :cube-1 (mc/->Pointer :cube-1 :ne)) => false
  (valid-end? :battlefield-1 :cube-1 (mc/->Pointer :cube-1 :se)) => false
  (valid-end? :battlefield-1 :cube-1 (mc/->Pointer :cube-1 :s)) => false
  (valid-end? :battlefield-1 :cube-1 (mc/->Pointer :cube-1 :sw)) => true
  (valid-end? :battlefield-1 :cube-1 (mc/->Pointer :cube-1 :nw)) => true))


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
    (valid-end? :battlefield :cube-1 pointer-1) => true))


 (let [pointer-1 {:cube :cube-1}]

   (forward-paths :battlefield pointer-1 1)
   => [[pointer-1]]

   (provided
    (forward-step pointer-1) => []
    (valid-end? :battlefield :cube-1 pointer-1) => true))


 (let [pointer-1 {:cube :cube-1}]

   (forward-paths :battlefield pointer-1 0)
   => []

   (provided
    (valid-end? :battlefield :cube-1 pointer-1) => false))


 (let [pointer-1 {:cube :cube-1}]

   (forward-paths :battlefield pointer-1 1)
   => []

   (provided
    (forward-step pointer-1) => []
    (valid-end? :battlefield :cube-1 pointer-1) => false))


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

    (valid-end? :battlefield :cube-1 pointer-1) => true

    (forward-step pointer-1) => [pointer-2 pointer-3 pointer-4]
    (valid-move? :battlefield :cube-1 pointer-2) => true
    (valid-move? :battlefield :cube-1 pointer-3) => true
    (valid-move? :battlefield :cube-1 pointer-4) => false

    (valid-end? :battlefield :cube-1 pointer-2) => true

    (valid-end? :battlefield :cube-1 pointer-3) => true)))


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

    (valid-move? :battlefield :cube-1 pointer-1-ne) => true
    (valid-move? :battlefield :cube-1 pointer-1-se) => true
    (valid-move? :battlefield :cube-1 pointer-1-s) => true
    (valid-move? :battlefield :cube-1 pointer-1-sw) => true
    (valid-move? :battlefield :cube-1 pointer-1-nw) => false

    (mc/step :cube-2-ne :ne) => :cube-3-ne
    (mc/step :cube-2-se :se) => :cube-3-se
    (mc/step :cube-2-s :s) => :cube-3-s
    (mc/step :cube-2-sw :sw) => :cube-3-sw

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
 "path events"

 (let [battlefield {:cube-1 :terrain-1
                    :cube-2 :terrain-2
                    :cube-3 :unit-1}

       unit {:unit/player 1
             :unit/id 2}]

   (path-events battlefield unit [(mc/->Pointer :cube-1 :n)
                                  (mc/->Pointer :cube-2 :n)
                                  (mc/->Pointer :cube-3 :n)])
   => [(mv/dangerous 1 2)]

   (provided
    (lt/dangerous? :terrain-1) => true
    (lt/dangerous? :terrain-2) => false
    (lt/dangerous? :unit-1) => false)))


(facts
 "show events"

 (show-events :battlefield :unit-1 [[:pointer-1] [:pointer-1 :pointer-2]])
 => {:pointer-1 :events-1
     :pointer-2 :events-2}

 (provided
  (path-events :battlefield :unit-1 [:pointer-1]) => :events-1
  (path-events :battlefield :unit-1 [:pointer-1 :pointer-2]) => :events-2))


(facts
 "show moves"

 (let [unit {:unit/facing :n
             :unit/M 5
             :unit/player 1}

       battlefield {:cube-1 unit}

       path-fn (constantly :paths-1)]

   (show-moves battlefield :cube-1 :hexes path-fn)
   => {:battlemap :battlemap-1
       :breadcrumbs :breadcrumbs-1
       :pointer->events :p->e-1}

   (provided
    (show-battlemap battlefield 1 :paths-1) => :battlemap-1

    (show-breadcrumbs battlefield :battlemap-1 1 :paths-1) => :breadcrumbs-1

    (show-events battlefield unit :paths-1) => :p->e-1)))


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


(facts
 "show march"

 (let [battlefield {:cube-1 {:unit/M 8}}]

   (show-march battlefield :cube-1)
   => {:battlemap {:cube-1 :battlemap-entry
                   :cube-2 :threat-entry}
       :threats? true}

   (provided
    (show-moves battlefield :cube-1 5 forward-paths)
    => {:battlemap {:cube-1 :battlemap-entry}}

    (show-threats battlefield :cube-1)
    => {:cube-2 :threat-entry})))


(facts
 "charge step"

 (charge-step (mc/->Pointer :cube-1 :n) :n)
 => [(mc/->Pointer :cube-1 :nw)
     (mc/->Pointer :cube-1 :ne)
     (mc/->Pointer :cube-2 :n)
     (mc/->Pointer :cube-2 :nw)
     (mc/->Pointer :cube-2 :ne)]

 (provided
  (mc/step :cube-1 :n)
  => :cube-2)


 (charge-step (mc/->Pointer :cube-1 :nw) :n)
 => [(mc/->Pointer :cube-1 :n)
     (mc/->Pointer :cube-2 :nw)
     (mc/->Pointer :cube-2 :n)]

 (provided
  (mc/step :cube-1 :nw)
  => :cube-2))


(facts
 "charge paths"

 (charge-paths :battlefield :start #{})
 => {}


 (let [start (mc/->Pointer :cube-1 :n)]

   (charge-paths :battlefield-1 start #{:cube-2})
   => {}

   (provided
    (l/move-unit :battlefield-1 :cube-1 start) => :battlefield-2
    (l/engaged-cubes :battlefield-2 :cube-1) => []
    (charge-step start :n) => []))


 (let [start (mc/->Pointer :cube-1 :n)]

   (charge-paths :battlefield-1 start #{:cube-2})
   => {[start] #{:cube-2}}

   (provided
    (l/move-unit :battlefield-1 :cube-1 start) => :battlefield-2
    (l/engaged-cubes :battlefield-2 :cube-1) => [:cube-2]
    (charge-step start :n) => []))


 (let [start (mc/->Pointer :cube-1 :n)]

   (charge-paths :battlefield-1 start #{:cube-3})
   => {}

   (provided
    (l/move-unit :battlefield-1 :cube-1 start) => :battlefield-2
    (l/engaged-cubes :battlefield-2 :cube-1) => [:cube-2]
    (charge-step start :n) => []))


 (let [start (mc/->Pointer :cube-1 :n)
       pointer-1 (mc/->Pointer :cube-2 :n)]

   (charge-paths :battlefield-1 start #{:cube-3 :cube-4})
   => {[start] #{:cube-3}}

   (provided
    (l/move-unit :battlefield-1 :cube-1 start) => :battlefield-2
    (l/engaged-cubes :battlefield-2 :cube-1) => [:cube-3]
    (charge-step start :n) => [pointer-1]
    (valid-move? :battlefield-1 :cube-1 pointer-1) => true

    (l/move-unit :battlefield-1 :cube-1 pointer-1) => :battlefield-3
    (l/engaged-cubes :battlefield-3 :cube-2) => [:cube-3]
    (charge-step pointer-1 :n) => []))


 (let [start (mc/->Pointer :cube-1 :n)]

   (charge-paths :battlefield-1 start #{:cube-2})
   => {}

   (provided
    (l/move-unit :battlefield-1 :cube-1 start) => :battlefield-2
    (l/engaged-cubes :battlefield-2 :cube-1) => [:cube-2 :cube-3]
    (charge-step start :n) => [])))


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
    (l/field-of-view battlefield :cube-1) => [:cube-2 :cube-3 :cube-4 :cube-5]
    (le/unit? :terrain-1) => false

    (le/unit? :unit-2) => true
    (l/enemies? unit-1 :unit-2) => false

    (le/unit? :unit-3) => true
    (l/enemies? unit-1 :unit-3) => true
    (mc/distance :cube-1 :cube-4) => 4

    (le/unit? :unit-4) => true
    (l/enemies? unit-1 :unit-4) => true
    (mc/distance :cube-1 :cube-5) => 3)))


(facts
 "charger?"

 (let [battlefield {:cube-1 {:unit/facing :n}}
       pointer (mc/->Pointer :cube-1 :n)]

   (charger? battlefield :cube-1) => false

   (provided
    (list-targets battlefield :cube-1) => :targets
    (charge-paths battlefield pointer :targets) => {}))


 (let [battlefield {:cube-1 {:unit/facing :n}}
       pointer (mc/->Pointer :cube-1 :n)]

   (charger? battlefield :cube-1) => true

   (provided
    (list-targets battlefield :cube-1) => :targets
    (charge-paths battlefield pointer :targets) => {:path-1 :targets-1})))


(facts
 "show targets"

 (show-targets :battlefield {[:pointer-1] #{:cube-1}
                             [:pointer-1 :pointer-2] #{:cube-1 :cube-2}})
 => {:pointer-1 :marked-1
     :pointer-2 :marked-2}

 (provided
  (l/show-cubes :battlefield #{:cube-1} :marked) => :marked-1
  (l/show-cubes :battlefield #{:cube-1 :cube-2} :marked) => :marked-2))


(facts
 "target ranges"

 (target-ranges {[:pointer-1] #{:cube-2 :cube-3}
                 [:pointer-1 :pointer-2]  #{:cube-2}}
                :cube-1)
 => {:pointer-1 3
     :pointer-2 2}

 (provided
  (mc/distance :cube-1 :cube-2) => 2
  (mc/distance :cube-1 :cube-3) => 3))


(facts
 "show charge"

 (let [battlefield {:cube-1 {:unit/facing :n
                             :unit/player 1}}
       start (mc/->Pointer :cube-1 :n)]

   (show-charge battlefield :cube-1)
   => {:battlemap {:cube-1 :unit-map-entry-1
                   :cube-2 :battlemap-entry-1}
       :breadcrumbs {:cube-3 :breadcrumbs-entry-1
                     :cube-4 :target-map-entry-1}
       :ranges :target-ranges}

   (provided
    (list-targets battlefield :cube-1)
    => :targets

    (charge-paths battlefield start :targets)
    => {:path-1 :target-1}

    (show-battlemap battlefield 1 [:path-1])
    => {:cube-2 :battlemap-entry-1}

    (show-breadcrumbs battlefield {:cube-2 :battlemap-entry-1} 1 [:path-1])
    => {:cube-3 :breadcrumbs-entry-1}

    (l/show-cubes battlefield [:cube-1] :selected)
    => {:cube-1 :unit-map-entry-1}

    (show-targets battlefield {:path-1 :target-1})
    => {:cube-4 :target-map-entry-1}

    (target-ranges {:path-1 :target-1} :cube-1) => :target-ranges)))
