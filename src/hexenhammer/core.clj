(ns hexenhammer.core
  (:require [compojure.core :refer [defroutes routes GET]]
            [hexenhammer.logic.battlefield.core :as lbc]
            [hexenhammer.view.setup :as vs]
            [ring.adapter.jetty :refer [run-jetty]]))


(def hexenhammer-state (atom (lbc/gen-initial-state 8 12)))


(defroutes setup-handler
  (GET "/setup/select" [] (vs/select @hexenhammer-state)))



(def hexenhammer-app
  (routes setup-handler))


;; (defonce server (run-jetty #'hexenhammer-app {:port 8080 :join? false}))
;; (.start server)
;; (.stop server)
