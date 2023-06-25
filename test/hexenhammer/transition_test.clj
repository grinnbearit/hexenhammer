(ns hexenhammer.unit-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.transition :refer :all]
            [hexenhammer.engine.component :as component]
            [hexenhammer.cube :as cube]))


(facts
 "gen battlefield cubes"

 (gen-battlefield-cubes 0 0)
 => []

 (gen-battlefield-cubes 1 2)
 => [(cube/->Cube 0 0 0)
     (cube/->Cube 1 0 -1)]

 (gen-battlefield-cubes 2 4)
 => [(cube/->Cube 0 0 0)
     (cube/->Cube 1 0 -1)
     (cube/->Cube 2 -1 -1)
     (cube/->Cube 3 -1 -2)

     (cube/->Cube 0 1 -1)
     (cube/->Cube 1 1 -2)
     (cube/->Cube 2 0 -2)
     (cube/->Cube 3 0 -3)])


(facts
 "gen initial state"

 (gen-initial-state 3 4)
 => {:game/phase :setup
     :map/rows 3
     :map/columns 4
     :map/battlefield {(cube/->Cube 0 0 0) {:hexenhammer/class :terrain
                                            :terrain/name "grass"
                                            :terrain/position (cube/->Cube 0 0 0)}
                       (cube/->Cube 1 0 -1) {:hexenhammer/class :terrain
                                             :terrain/name "grass"
                                             :terrain/position (cube/->Cube 1 0 -1)}}}

 (provided
  (gen-battlefield-cubes 3 4)
  => [(cube/->Cube 0 0 0)
      (cube/->Cube 1 0 -1)]))


(facts
 "unselect cube"

 (unselect-cube {:map/battlefield {}
                 :map/selected (cube/->Cube 0 0 0)})
 => {:map/battlefield {}})


(facts
 "select cube"

 (select-cube {} (cube/->Cube 0 0 0))
 => {:map/selected (cube/->Cube 0 0 0)}


 (select-cube {:map/selected (cube/->Cube 0 0 0)}
              (cube/->Cube 0 0 0))
 => {}

 (provided
  (unselect-cube {:map/selected (cube/->Cube 0 0 0)})
  => {}))


(facts
 "add unit"

 (add-unit {:map/state :state} :cube :player 0 :facing :e)
 => {:map/players {0 {"infantry" {:counter 1}}}
     :map/battlefield {:cube [:infantry :cube :e]}}

 (provided
  (component/gen-infantry 0 0 :cube :e) => [:infantry :cube :e]
  (unselect-cube {:map/state :state}) => {}))


(facts
 "remove unit"

 (let [state {:map/battlefield {:cube [:unit :cube]}}]

   (remove-unit state :cube)
   => {:map/battlefield {:cube [:grass :cube]}}

   (provided
    (unselect-cube state) => state
    (component/gen-grass :cube) => [:grass :cube])))


(facts
 "to movement"

 (to-movement {:game/phase :setup})
 => {:game/phase :movement})
