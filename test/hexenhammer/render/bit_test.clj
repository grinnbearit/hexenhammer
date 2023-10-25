(ns hexenhammer.render.bit-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.render.bit :refer :all]))


(facts
 "phase -> url"

 (phase->url "/prefix/" [:phase :subphase])
 => "/prefix/phase/subphase"

 (phase->url "/prefix/" [:phase] {:x 1 :y 2})
 => "/prefix/phase?x=1&y=2")


(facts
 "int->roman"

 (int->roman 4) => "iv")


(facts
 "unit-key -> str"

 (unit-key->str {:unit/player 1
                 :unit/name "unit"
                 :unit/id 2})
 => "P1 - unit (ii)")


(facts
 "player -> str"

 (player->str 1)
 => "Player - 1")
