(ns hexenhammer.render.internal-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.render.internal :refer :all]
            [hexenhammer.cube :as cube]))


(facts
 "size -> dim"

 (size->dim 0 0 :width 200 :height 100)
 => (throws AssertionError)

 (size->dim 1 1 :width 200 :height 100)
 => {:width 200
     :height 100}

 (size->dim 2 1 :width 200 :height 100)
 => {:width 200
     :height 200}

 (size->dim 2 2 :width 200 :height 100)
 => {:width 350
     :height 250})


(facts
 "cube -> point"

 (cube->point (cube/->Cube 0 0 0) :width 200 :height 100)
 => [100 50]

 (cube->point (cube/->Cube 1 -1 0) :width 200 :height 100)
 => [250 0]

 (cube->point (cube/->Cube 1 0 -1) :width 200 :height 100)
 => [250 100])


(facts
 "svg translate"

 (svg-translate (cube/->Cube 3 2 1)
                [:g {}] :width 200 :height 100)
 => [:g {:transform "translate(10.50, 20.50)"}]

 (provided
  (cube->point (cube/->Cube 3 2 1) :width 200 :height 100)
  => [10.5 20.5])

 (svg-translate (cube/->Cube 3 2 1)
                [:g {:transform "rotate(30)"}] :width 200 :height 100)
 => [:g {:transform "translate(10.50, 20.50) rotate(30)"}]

 (provided
  (cube->point (cube/->Cube 3 2 1) :width 200 :height 100)
  => [10.5 20.5]))


(facts
 "svg rotate"

 (svg-rotate 30 [:g {}])
 => [:g {:transform "rotate(30.00)"}]

 (svg-rotate 30 [:g {:transform "rotate(60.00)"}])
 => [:g {:transform "rotate(30.00) rotate(60.00)"}])


(facts
 "svg scale"

 (svg-scale 1/2 [:g {}])
 => [:g {:transform "scale(0.50)"}]

 (svg-scale 1/2 [:g {:transform "scale(0.90)"}])
 => [:g {:transform "scale(0.50) scale(0.90)"}])


(facts
 "points -> str"

 (points->str [[1/2 1] [2 3] [4 5]])
 => "0.5,1.0 2.0,3.0 4.0,5.0")


(facts
 "gen hexpoints"

 (gen-hexpoints :width 200 :height 100)
 => [[-100 0] [-50 -50] [50 -50]
     [100 0] [50 50] [-50 50]])


(facts
 "svg hexagon"

 (svg-hexagon :width 200 :height 100)
 => [:polygon {:points "hexpoints" :class ""}]

 (provided
  (gen-hexpoints :width 200 :height 100) => :hexpoints
  (points->str :hexpoints) => "hexpoints")


 (svg-hexagon :width 200 :height 100 :classes ["terrain" "grass"])
 => [:polygon {:points "hexpoints" :class "terrain grass"}]

 (provided
  (gen-hexpoints :width 200 :height 100) => :hexpoints
  (points->str :hexpoints) => "hexpoints"))


(facts
 "svg text"

 (svg-text 0 "" :font-size 12)
 => [:text {:fill "white" :font-family "monospace" :font-size "12" :x -3 :y 3} ""]


 (svg-text 1 "0" :font-size 12)
 => [:text {:fill "white" :font-family "monospace" :font-size "12" :x -6 :y 15} "0"]

 (svg-text -1 "0" :font-size 12)
 => [:text {:fill "white" :font-family "monospace" :font-size "12" :x -6 :y -9} "0"])


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
 => [:polyline {:points :points-str :transform "rotate" :stroke "white" :fill "white"}]

 (provided
  (gen-chevpoints :width 200 :height 100) => :chevpoints
  (points->str :chevpoints) => :points-str
  (svg-rotate 180 [:polyline {:points :points-str :stroke "white" :fill "white"}])
  => [:polyline {:points :points-str :transform "rotate" :stroke "white" :fill "white"}])


 (svg-chevron :se :width 200 :height 100)
 => [:polyline {:points :points-str :transform "rotate" :stroke "white" :fill "white"}]

 (provided
  (gen-chevpoints :width 200 :height 100) => :chevpoints
  (points->str :chevpoints) => :points-str
  (svg-rotate 300 [:polyline {:points :points-str :stroke "white" :fill "white"}])
  => [:polyline {:points :points-str :transform "rotate" :stroke "white" :fill "white"}]))
