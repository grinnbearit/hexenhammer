(ns hexenhammer.core
  (:require [compojure.core :refer [defroutes GET]]
            [hexenhammer.model.core :as model]
            [hexenhammer.model.cube :as cube]
            [hexenhammer.view.html :as view]
            [hexenhammer.controller.core :as controller]
            [ring.util.response :refer [redirect]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.adapter.jetty :refer [run-jetty]]))


(def hexenhammer-state
  (atom (model/gen-initial-state 8 12)))


(defn render-handler
  [request]
  (view/render @hexenhammer-state))


(defn select-handler
  [{params :query-params}]
  (let [{:strs [q r s]} params
        cube (cube/->Cube (Integer/parseInt q)
                          (Integer/parseInt r)
                          (Integer/parseInt s))]
    (do (swap! hexenhammer-state controller/select cube)
        (redirect "/"))))


(defroutes hexenhammer-handler
  (GET "/" [] render-handler)
  (GET "/select" [] select-handler)
  (GET "/favicon.ico" [] ""))


(def hexenhammer-app
  (-> hexenhammer-handler
      wrap-params))


;; (defonce server (run-jetty #'hexenhammer-app {:port 8080 :join? false}))
;; (.start server)
;; (.stop server)
