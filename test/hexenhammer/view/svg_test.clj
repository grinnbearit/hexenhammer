(ns hexenhammer.view.svg-test
  (:require [midje.sweet :refer :all]
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
