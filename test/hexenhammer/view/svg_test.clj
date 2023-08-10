(ns hexenhammer.view.svg-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as cube]
            [hexenhammer.view.svg :refer :all]))


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
 "translate"

 (translate [:g {}] :cube-1 :width :width-1 :height :height-1)
 => [:g {:transform "translate(10.50, 20.50)"}]

 (provided
  (cube->point :cube-1 :width :width-1 :height :height-1)
  => [10.5 20.5])

 (translate [:g {:transform "rotate(30)"}] :cube-2 :width :width-2 :height :height-2)
 => [:g {:transform "translate(10.50, 20.50) rotate(30)"}]

 (provided
  (cube->point :cube-2 :width :width-2 :height :height-2)
  => [10.5 20.5]))


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
 "hexagon"

 (hexagon :width :width-1 :height :height-1)
 => [:polygon {:points "hexpoints-1"}]

 (provided
  (gen-hexpoints :width :width-1 :height :height-1) => :hexpoints-1
  (points->str :hexpoints-1) => "hexpoints-1"))


(facts
 "add-classes"

 (add-classes [:element-1 {}] ["class-1" "class-2"])
 => [:element-1 {:class "class-1 class-2"}]

 (add-classes [:element-1 {:class "class-1"}] ["class-2" "class-3"])
 => [:element-1 {:class "class-1 class-2 class-3"}])


(facts
 "anchor"

 (anchor :element-1 :href-1)
 => [:a {:href :href-1} :element-1])


(facts
 "selectable"

 (selectable :element-1 {:q 1 :r 1 :s 1})
 => [:a {:href "/select?q=1&r=1&s=1"} :element-1])


(facts
 "movable"

 (movable :element-1 {:q 1 :r 1 :s 1} :n)
 => [:a {:href "/move?q=1&r=1&s=1&facing=n"} :element-1])


(facts
 "scale"

 (scale [:g {}] 9/10)
 => [:g {:transform "scale(0.90)"}]

 (scale [:g {:transform "rotate(30)"}] 9/10)
 => [:g {:transform "scale(0.90) rotate(30)"}])


(facts
 "rotate"

 (rotate [:element {}] 30)
 => [:element {:transform "rotate(30.00)"}]

 (rotate [:element {:transform "scale(0.90)"}] 30)
 => [:element {:transform "rotate(30.00) scale(0.90)"}])


(facts
 "gen chevpoints"

 (gen-chevpoints :width 200 :height 100)
 => [[0 50] [-10 45] [10 45]])


(facts
 "gen arrpoints"

 (gen-arrpoints :width 200 :height 100)
 => [[0 50] [-20 35] [20 35]])


(facts
 "wrap pointer"

 (wrap-pointer :n :points-1)
 => :rotated-1

 (provided
  (points->str :points-1)
  => :points-str-1

  (rotate [:polygon {:points :points-str-1 :stroke "white" :fill "white"}] 180)
  => :rotated-1))


(facts
 "chevron"

 (chevron :facing-1 :width 200 :height 100)
 => :wrapped-1

 (provided
  (gen-chevpoints :width 200 :height 100) => :chevpoints-1
  (wrap-pointer :facing-1 :chevpoints-1) => :wrapped-1))


(facts
 "arrow"

 (arrow :facing-1 :width 200 :height 100)
 => :wrapped-1

 (provided
  (gen-arrpoints :width 200 :height 100) => :arrpoints-1
  (wrap-pointer :facing-1 :arrpoints-1) => :wrapped-1))


(facts
 "text"

 (text "" 0 :font-size 12)
 => [:text {:fill "white" :font-family "monospace" :font-size "12" :x -3 :y 3} ""]


 (text "0" 1 :font-size 12)
 => [:text {:fill "white" :font-family "monospace" :font-size "12" :x -6 :y 15} "0"]

 (text "0" -1 :font-size 12)
 => [:text {:fill "white" :font-family "monospace" :font-size "12" :x -6 :y -9} "0"])
