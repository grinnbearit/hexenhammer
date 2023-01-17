(ns hexenhammer.svg.internal-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.svg.internal :refer :all]))


(facts
 "size -> dim"

 (size->dim 0 :width 200 :height 100)
 => {:width 200 :height 100}

 (size->dim 1 :width 200 :height 100)
 => {:width 500 :height 300})


(facts
 "gen hexpoints"

 (gen-hexpoints :width 200 :height 100)
 => [[-100 0] [-50 -50] [50 -50]
     [100 0] [50 50] [-50 50]])


(facts
 "points -> str"

 (points->str [[1/2 1] [2 3] [4 5]])
 => "0.5,1.0 2.0,3.0 4.0,5.0")


(facts
 "cube -> points"

 (cube->points 0 0 0 :width 200 :height 100)
 => [0 0]

 (cube->points 1 0 0 :width 200 :height 100)
 => (throws AssertionError)

 (cube->points 0 1 -1 :width 200 :height 100)
 => [0 100]

 (cube->points 0 -1 1 :width 200 :height 100)
 => [0 -100]

 (cube->points 1 -1 0 :width 200 :height 100)
 => [150 -50]

 (cube->points 1 0 -1 :width 200 :height 100)
 => [150 50])


(facts
 "translate points"

 (translate-points 0 0 0 :width 200 :height 100)
 => [100 50]

 (provided
  (size->dim 0 :width 200 :height 100)
  => {:width 200 :height 100})

 (translate-points 0 300 300 :width 200 :height 100)
 => (throws clojure.lang.ExceptionInfo)

 (provided
  (size->dim 0 :width 200 :height 100)
  => {:width 200 :height 100}))


(facts
 "translate cube"

 (translate-cube 4 3 2 1 :width 200 :height 100)
 => [:xt :yt]

 (provided
  (cube->points 3 2 1 :width 200 :height 100) => [:x :y]
  (translate-points 4 :x :y :width 200 :height 100) => [:xt :yt]))


(facts
 "svg translate"

 (svg-translate 4 3 2 1 [:g {}] :width 200 :height 100)
 => [:g {:transform "translate(10.50, 20.50)"}]

 (provided
  (translate-cube 4 3 2 1 :width 200 :height 100)
  => [10.5 20.5])

 (svg-translate 4 3 2 1 [:g {:transform "rotate(30)"}] :width 200 :height 100)
 => [:g {:transform "translate(10.50, 20.50) rotate(30)"}]

 (provided
  (translate-cube 4 3 2 1 :width 200 :height 100)
  => [10.5 20.5]))


(facts
 "svg hexagon"

 (svg-hexagon :width 200 :height 100)
 => [:polygon {:points "hexpoints"
               :fill "green" :stroke "black"}]

 (provided
  (gen-hexpoints :width 200 :height 100) => :hexpoints
  (points->str :hexpoints) => "hexpoints")


 (svg-hexagon :fill "red" :stroke "green" :width 200 :height 100)
 => [:polygon {:points "hexpoints"
               :fill "red" :stroke "green"}]

 (provided
  (gen-hexpoints :width 200 :height 100) => :hexpoints
  (points->str :hexpoints) => "hexpoints") )


(facts
 "svg text"

 (svg-text 0 "" :font-size 12)
 => [:text {:font-family "monospace" :font-size "12" :x -3 :y 3} ""]


 (svg-text 1 "0" :font-size 12)
 => [:text {:font-family "monospace" :font-size "12" :x -6 :y 15} "0"]

 (svg-text -1 "0" :font-size 12)
 => [:text {:font-family "monospace" :font-size "12" :x -6 :y -9} "0"])


(facts
 "svg coordinates"
 (svg-coordinates 3 2 1)
 => [:text "[3, 2, 1]"]

 (provided
  (svg-text 0 "[3, 2, 1]")
  => [:text "[3, 2, 1]"]))
