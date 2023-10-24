(ns hexenhammer.core
  (:require [compojure.core :refer [defroutes routes GET POST wrap-routes]]
            [hexenhammer.logic.battlefield.core :as lb]
            [hexenhammer.controller.core :as c]
            [hexenhammer.controller.setup :as cs]
            [hexenhammer.view.core :as v]
            [hexenhammer.view.setup :as vs]
            [hexenhammer.web.server :as ws]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]))


(def hexenhammer-state (atom (lb/gen-initial-state 8 12)))


(defroutes view-handler
  (GET "/" [] (v/select @hexenhammer-state))
  (GET "/favicon.ico" [] "")
  (GET "/setup/select-hex" [] (vs/select @hexenhammer-state))
  (GET "/setup/add-unit" [] (vs/add-unit @hexenhammer-state))
  (GET "/setup/remove-unit" [] (vs/remove-unit @hexenhammer-state)))


(defroutes controller-handler
  (POST "/to-setup" [] (swap! hexenhammer-state c/to-setup))
  (POST "/setup/add-unit" [player facing M Ld R] (swap! hexenhammer-state cs/add-unit
                                                        (Integer/parseInt player)
                                                        (keyword facing)
                                                        (Integer/parseInt M)
                                                        (Integer/parseInt Ld)
                                                        (Integer/parseInt R)))
  (POST "/setup/remove-unit" [] (swap! hexenhammer-state cs/remove-unit))
  (POST "/setup/swap-terrain" [terrain] (swap! hexenhammer-state cs/swap-terrain (keyword terrain))))


(defroutes select-handler
  (GET "/select/setup/select-hex" [cube] (swap! hexenhammer-state cs/select-hex cube))
  (GET "/select/setup/add-unit" [cube] (swap! hexenhammer-state cs/select-add-unit cube))
  (GET "/select/setup/remove-unit" [cube] (swap! hexenhammer-state cs/select-remove-unit cube)))


(def hexenhammer-app
  (-> (routes view-handler
              (-> controller-handler
                  (wrap-routes ws/wrap-redirect-phase)
                  (wrap-routes wrap-params))
              (-> select-handler
                  (wrap-routes ws/wrap-redirect-phase)
                  (wrap-routes ws/wrap-select)
                  (wrap-routes wrap-params)))))


;; (defonce server (run-jetty #'hexenhammer-app {:port 8080 :join? false}))
;; (.start server)
;; (.stop server)
