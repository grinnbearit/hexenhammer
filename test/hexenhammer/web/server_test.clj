(ns hexenhammer.web.server-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.web.server :refer :all]))


(facts
 "wrap redirect phase"

 (letfn [(handler [request]
           {:game/phase [:phase :subphase]})]

   ((wrap-redirect-phase handler) :request)
   => {:body "" :headers {"Location" "/phase/subphase"} :status 302}))


(facts
 "wrap select"

 (let [request {:params {"q" "1" "r" "2" "s" "-3"}}]

   ((wrap-select identity) request)
   => (assoc-in request [:params "cube"] (lc/->Cube 1 2 -3))))


(facts
 "wrap move"

 (let [request {:params {"facing" "n" "q" "1" "r" "2" "s" "-3"}}]

   ((wrap-move identity) request)
   => (assoc-in request [:params "pointer"] (lc/->Pointer (lc/->Cube 1 2 -3) :n))))
