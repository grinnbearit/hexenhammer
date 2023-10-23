(ns hexenhammer.render.svg-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.render.svg :refer :all]))


(facts
 "size -> dim"

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

 (cube->point (lc/->Cube 0 0 0) :width 200 :height 100)
 => [100 50]

 (cube->point (lc/->Cube 1 -1 0) :width 200 :height 100)
 => [250 0]

 (cube->point (lc/->Cube 1 0 -1) :width 200 :height 100)
 => [250 100])


(facts
 "points -> str"

 (points->str [[1/2 1] [2 3] [4 5]])
 => "0.5,1.0 2.0,3.0 4.0,5.0")


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
 "phase -> url"

 (phase->url "/prefix/" [:phase :subphase])
 => "/prefix/phase/subphase"

 (phase->url "/prefix/" [:phase] {:x 1 :y 2})
 => "/prefix/phase?x=1&y=2")


(facts
 "selectable"

 (selectable :element-1 :phase :form)
 => [:a {:href :url} :element-1]

 (provided
  (phase->url "/select/" :phase :form) => :url))


(facts
 "if selectable"

 (if-selectable :element-1 :presentation-1 :phase-1 :cube-1)
 => :element-1


 (for [presentation [:selectable :silent-selectable :selected]]

   (if-selectable :element-1 presentation :phase-1 :cube-1)
   => :selectable

   (provided
    (selectable :element-1 :phase-1 :cube-1)
    => :selectable)))
