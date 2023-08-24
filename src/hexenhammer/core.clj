(ns hexenhammer.core
  (:require [compojure.core :refer [defroutes routes GET POST]]
            [hexenhammer.model.core :as model]
            [hexenhammer.model.cube :as cube]
            [hexenhammer.view.html :as view]
            [hexenhammer.controller.core :as controller]
            [hexenhammer.server :refer [wrap-redirect-home]]
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
    (swap! hexenhammer-state controller/select cube)))


(defn move-handler
  [{{:strs [q r s facing]} :params}]
  (let [cube (cube/->Cube (Integer/parseInt q)
                          (Integer/parseInt r)
                          (Integer/parseInt s))
        pointer (cube/->Pointer cube
                                (keyword facing))]
    (swap! hexenhammer-state controller/move pointer)))


(defn setup-add-unit-handler
  [{{:strs [player facing M Ld]} :params}]
  (let [player (Integer/parseInt player)
        facing (keyword facing)
        M (Integer/parseInt M)
        Ld (Integer/parseInt Ld)]
    (swap! hexenhammer-state controller/add-unit player facing
           {:M M :Ld Ld})))


(defroutes home-handler
  (GET "/" [] render-handler))


(defroutes api-handler
  (GET "/select" [] select-handler)
  (GET "/move" [] move-handler)
  (GET "/movement/:movement" [movement] (swap! hexenhammer-state controller/movement-transition (keyword movement)))
  (GET "/favicon.ico" [] "")
  (POST "/setup/add-unit" [] setup-add-unit-handler)
  (POST "/setup/remove-unit" [] (swap! hexenhammer-state controller/remove-unit))
  (POST "/setup/to-movement" [] (swap! hexenhammer-state controller/to-movement))
  (POST "/movement/skip-movement" [] (swap! hexenhammer-state controller/skip-movement))
  (POST "/movement/finish-movement" [] (swap! hexenhammer-state controller/finish-movement))
  (POST "/movement/test-leadership" [] (swap! hexenhammer-state controller/test-march!)))


(def hexenhammer-app
  (routes home-handler
          (-> api-handler
              wrap-params
              wrap-redirect-home)))


;; (defonce server (run-jetty #'hexenhammer-app {:port 8080 :join? false}))
;; (.start server)
;; (.stop server)
