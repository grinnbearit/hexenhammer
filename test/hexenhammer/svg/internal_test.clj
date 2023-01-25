(ns hexenhammer.svg.internal-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.svg.internal :refer :all]
            [hexenhammer.cube :as cube]))


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
 "cube -> point"

 (cube->point (cube/->Cube 0 0 0) :width 200 :height 100)
 => [0 0]

 (cube->point (cube/->Cube 0 1 -1) :width 200 :height 100)
 => [0 100]

 (cube->point (cube/->Cube 0 -1 1) :width 200 :height 100)
 => [0 -100]

 (cube->point (cube/->Cube 1 -1 0) :width 200 :height 100)
 => [150 -50]

 (cube->point (cube/->Cube 1 0 -1) :width 200 :height 100)
 => [150 50])


(facts
 "translate point"

 (translate-point 0 [0 0] :width 200 :height 100)
 => [100 50]

 (provided
  (size->dim 0 :width 200 :height 100)
  => {:width 200 :height 100})

 (translate-point 0 [300 300] :width 200 :height 100)
 => (throws clojure.lang.ExceptionInfo)

 (provided
  (size->dim 0 :width 200 :height 100)
  => {:width 200 :height 100}))


(facts
 "translate cube"

 (translate-cube 4 (cube/->Cube 3 2 1) :width 200 :height 100)
 => [:xt :yt]

 (provided
  (cube->point (cube/->Cube 3 2 1) :width 200 :height 100) => [:x :y]
  (translate-point 4 [:x :y] :width 200 :height 100) => [:xt :yt]))


(facts
 "svg translate"

 (svg-translate 4 (cube/->Cube 3 2 1) [:g {}] :width 200 :height 100)
 => [:g {:transform "translate(10.50, 20.50)"}]

 (provided
  (translate-cube 4 (cube/->Cube 3 2 1) :width 200 :height 100)
  => [10.5 20.5])

 (svg-translate 4 (cube/->Cube 3 2 1) [:g {:transform "rotate(30)"}] :width 200 :height 100)
 => [:g {:transform "translate(10.50, 20.50) rotate(30)"}]

 (provided
  (translate-cube 4 (cube/->Cube 3 2 1) :width 200 :height 100)
  => [10.5 20.5]))


(facts
 "svg rotate"

 (svg-rotate 30 [:g {}])
 => [:g {:transform "rotate(30.00)"}]

 (svg-rotate 30 [:g {:transform "rotate(60.00)"}])
 => [:g {:transform "rotate(30.00) rotate(60.00)"}])


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
 (svg-coordinates (cube/->Cube 3 2 1))
 => [:text "[3, 2, 1]"]

 (provided
  (svg-text 0 "[3, 2, 1]")
  => [:text "[3, 2, 1]"]))


(facts
 "gen chepoints"

 (gen-chevpoints :width 200 :height 100)
 => [[0 50] [-10 45] [10 45]])


(facts
 "svg chevron"

 (svg-chevron :n :width 200 :height 100)
 => [:polyline {:points :points-str :transform "rotate" :stroke "black"}]

 (provided
  (gen-chevpoints :width 200 :height 100) => :chevpoints
  (points->str :chevpoints) => :points-str
  (svg-rotate 180 [:polyline {:points :points-str :stroke "black"}])
  => [:polyline {:points :points-str :transform "rotate" :stroke "black"}])


 (svg-chevron :se :width 200 :height 100)
 => [:polyline {:points :points-str :transform "rotate" :stroke "black"}]

 (provided
  (gen-chevpoints :width 200 :height 100) => :chevpoints
  (points->str :chevpoints) => :points-str
  (svg-rotate 300 [:polyline {:points :points-str :stroke "black"}])
  => [:polyline {:points :points-str :transform "rotate" :stroke "black"}]))
