(ns hexenhammer.svg.internal-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.svg.internal :refer :all]))


(facts
 "size -> dim"

 (size->dim 0)
 => {:width 120 :height 104}

 (size->dim 1)
 => {:width 300 :height 312})


(facts
 "gen hexpoints"

 (gen-hexpoints 60 52)
 => [[0 52] [30 0] [90 0]
     [120 52] [90 104] [30 104]]

 (gen-hexpoints 70 72)
 => [[10 72] [40 20] [100 20]
     [130 72] [100 124] [40 124]])


(facts
 "points -> str"

 (points->str [[10 20] [30 40] [50 60]])
 => "10,20 30,40 50,60")


(facts
 "cube -> points"

 (cube->points 0 0 0) => [0 0]

 (cube->points 1 0 0) => (throws AssertionError)

 (cube->points 0 1 -1) => [0 104]

 (cube->points 0 -1 1) => [0 -104]

 (cube->points 1 -1 0) => [90 -52]

 (cube->points 1 0 -1) => [90 52])


(facts
 "translate points"

 (translate-points 0 0 0) => [60 52]
 (translate-points 1 0 0) => [150 156])


(facts
 "svg hexagon"

 (svg-hexagon 0 0 0 0)
 => [:polygon {:points "0,52 30,0 90,0 120,52 90,104 30,104"
               :fill "green" :stroke "black"}]

 (svg-hexagon 0 0 0 0 :colour "red" :stroke "green")
 => [:polygon {:points "0,52 30,0 90,0 120,52 90,104 30,104"
               :fill "red" :stroke "green"}])
