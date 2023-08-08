(ns hexenhammer.core
  (:require [compojure.core :refer [defroutes GET POST]]
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
  [{{:strs [q r s]} :params}]
  (let [cube (cube/->Cube (Integer/parseInt q)
                          (Integer/parseInt r)
                          (Integer/parseInt s))]
    (do (swap! hexenhammer-state controller/select cube)
        (redirect "/"))))


(defn setup-add-unit-handler
  [{{:strs [player facing]} :params}]
  (let [player (Integer/parseInt player)
        facing (keyword facing)]
    (do (swap! hexenhammer-state controller/add-unit player facing)
        (redirect "/"))))


(defroutes hexenhammer-handler
  (GET "/" [] render-handler)
  (GET "/select" [] select-handler)
  (GET "/favicon.ico" [] "")
  (POST "/setup/add-unit" [] setup-add-unit-handler)
  (POST "/setup/remove-unit" [] (do (swap! hexenhammer-state controller/remove-unit) (redirect "/")))
  (POST "/setup/to-movement" [] (do (swap! hexenhammer-state controller/to-movement) (redirect "/")))
  (POST "/movement/skip-movement" [] (do (swap! hexenhammer-state controller/skip-movement) (redirect "/"))))


(def hexenhammer-app
  (-> hexenhammer-handler
      wrap-params))


;; (defonce server (run-jetty #'hexenhammer-app {:port 8080 :join? false}))
;; (.start server)
;; (.stop server)
