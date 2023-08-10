(ns hexenhammer.server-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.server :refer :all]))


(facts
 "wrap redirect home"

 (let [state (atom 1)
       handler (fn [request] (swap! state inc))]

   ((wrap-redirect-home handler) :request)
   => {:body "" :headers {"Location" "/"} :status 302}

   @state => 2))
