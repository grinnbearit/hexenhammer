(ns hexenhammer.view.entity
  (:require [midje.sweet :refer :all]
            [hexenhammer.view.svg :as svg]
            [hexenhammer.view.entity :refer :all]))


(facts
 "render terrain"

 (render {:entity/class :terrain
          :entity/cube :cube-1})
 => [:hexagon {:class "terrain"} :cube-1]

 (provided
  (svg/hexagon) => [:hexagon {}]

  (svg/translate [:hexagon {:class "terrain"}] :cube-1)
  => [:hexagon {:class "terrain"} :cube-1])


 (render {:entity/class :terrain
          :entity/cube :cube-1
          :entity/interaction :selectable
          :entity/presentation :selected})

 => [:hexagon {:class "terrain selected"} :cube-1 :cube-1]

 (provided
  (svg/hexagon) => [:hexagon {}]

  (svg/translate [:hexagon {:class "terrain"}] :cube-1)
  => [:hexagon {:class "terrain"} :cube-1]

  (svg/selectable [:hexagon {:class "terrain selected"} :cube-1] :cube-1)
  => [:hexagon {:class "terrain selected"} :cube-1 :cube-1]))


(facts
 "render unit"

 (render {:entity/class :unit
          :entity/cube :cube-1
          :unit/player 1})
 => [:hexagon {:class "unit player-1"} :cube-1]

 (provided
  (svg/hexagon) => [:hexagon {}]

  (svg/translate [:hexagon {:class "unit player-1"}] :cube-1)
  => [:hexagon {:class "unit player-1"} :cube-1])


 (render {:entity/class :unit
          :entity/cube :cube-1
          :unit/player 1
          :entity/interaction :selectable
          :entity/presentation :selected})

 => [:hexagon {:class "unit player-1 selected"} :cube-1 :cube-1]

 (provided
  (svg/hexagon) => [:hexagon {}]

  (svg/translate [:hexagon {:class "unit player-1"}] :cube-1)
  => [:hexagon {:class "unit player-1"} :cube-1]

  (svg/selectable [:hexagon {:class "unit player-1 selected"} :cube-1] :cube-1)
  => [:hexagon {:class "unit player-1 selected"} :cube-1 :cube-1]))