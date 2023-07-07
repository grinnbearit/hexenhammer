(ns hexenhammer.core
  (:require [compojure.core :refer [defroutes GET]]
            [hexenhammer.model.core :as model]
            [hexenhammer.view.html :as view]
            [ring.middleware.params :refer [wrap-params]]
            [ring.adapter.jetty :refer [run-jetty]]))


(def hexenhammer-state
  (atom (model/gen-initial-state 8 12)))


(defn render-handler
  [request]
  (view/render @hexenhammer-state))


(defroutes hexenhammer-handler
  (GET "/" [] render-handler))


(def hexenhammer-app
  (-> hexenhammer-handler
      wrap-params))


;; (defonce server (run-jetty #'hexenhammer-app {:port 8080 :join? false}))
;; (.start server)
;; (.stop server)
