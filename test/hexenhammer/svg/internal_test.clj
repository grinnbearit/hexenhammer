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
 (translate-points 1 0 0) => [150 156]
 (translate-points 1 300 300) => (throws clojure.lang.ExceptionInfo))


(facts
 "translate cube"

 (translate-cube 4 3 2 1) => [:xt :yt]

 (provided
  (cube->points 3 2 1) => [:x :y]
  (translate-points 4 :x :y) => [:xt :yt]))


(facts
 "svg hexagon"

 (svg-hexagon 0 0 0 0)
 => [:polygon {:points "hexpoints"
               :fill "green" :stroke "black"}]

 (provided
  (translate-cube 0 0 0 0) => [:x :y]
  (gen-hexpoints :x :y) => :hexpoints
  (points->str :hexpoints) => "hexpoints")


 (svg-hexagon 0 0 0 0 :fill "red" :stroke "green")
 => [:polygon {:points "hexpoints"
               :fill "red" :stroke "green"}]

 (provided
  (translate-cube 0 0 0 0) => [:x :y]
  (gen-hexpoints :x :y) => :hexpoints
  (points->str :hexpoints) => "hexpoints"))


(facts
 "svg text"

 (svg-text 0 0 0 0 0 "")
 => [:text {:font-family "monospace" :font-size "12" :x -3 :y 3} ""]

 (provided
  (translate-cube 0 0 0 0)
  => [0 0])


 (svg-text 0 0 0 0 1 "0")
 => [:text {:font-family "monospace" :font-size "12" :x -6 :y 15} "0"]

 (provided
  (translate-cube 0 0 0 0)
  => [0 0])


 (svg-text 0 0 0 0 -1 "0")
 => [:text {:font-family "monospace" :font-size "12" :x -6 :y -9} "0"]

 (provided
  (translate-cube 0 0 0 0)
  => [0 0]))


(facts
 "svg coordinates"

 (svg-coordinates 2 0 0 0)
 => [:svg-text 2 0 0 0 0 "[0, 0, 0]"]

 (provided
  (svg-text 2 0 0 0 0 "[0, 0, 0]") => [:svg-text 2 0 0 0 0 "[0, 0, 0]"]))


(facts
 "svg unit"

 (svg-unit 2 0 0 0 "warrior" 1 12)
 => [[:svg-text 2 0 0 0 -1 "warrior - 1"]
     [:svg-text 2 0 0 0 1 "(12)"]]

 (provided
  (svg-text 2 0 0 0 -1 "warrior - 1") => [:svg-text 2 0 0 0 -1 "warrior - 1"]
  (svg-text 2 0 0 0 1 "(12)") => [:svg-text 2 0 0 0 1 "(12)"]))
