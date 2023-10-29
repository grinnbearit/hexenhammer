(ns hexenhammer.core
  (:require [compojure.core :refer [defroutes routes GET POST wrap-routes]]
            [hexenhammer.logic.battlefield.core :as lb]
            [hexenhammer.controller.core :as c]
            [hexenhammer.controller.setup :as cs]
            [hexenhammer.controller.movement :as cm]
            [hexenhammer.view.core :as v]
            [hexenhammer.view.setup :as vs]
            [hexenhammer.view.movement :as vm]
            [hexenhammer.web.server :as ws]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]))


(def hexenhammer-state (atom (lb/gen-initial-state 8 12)))


(defroutes view-handler
  (GET "/" [] (v/select-hex @hexenhammer-state))
  (GET "/favicon.ico" [] "")
  (GET "/setup/select-hex" [] (vs/select-hex @hexenhammer-state))
  (GET "/setup/add-unit" [] (vs/add-unit @hexenhammer-state))
  (GET "/setup/remove-unit" [] (vs/remove-unit @hexenhammer-state))

  (GET "/movement/select-hex" [] (vm/select-hex @hexenhammer-state))
  (GET "/movement/reform" [] (vm/reform @hexenhammer-state))
  (GET "/movement/forward" [] (vm/forward @hexenhammer-state))
  (GET "/movement/reposition" [] (vm/reposition @hexenhammer-state))
  (GET "/movement/march" [] (vm/march @hexenhammer-state)))


(defroutes controller-handler
  (POST "/to-setup" [] (swap! hexenhammer-state c/to-setup))
  (POST "/to-movement" [] (swap! hexenhammer-state c/to-movement))
  (POST "/setup/add-unit" [player facing M Ld R] (swap! hexenhammer-state cs/add-unit
                                                        (Integer/parseInt player)
                                                        (keyword facing)
                                                        (Integer/parseInt M)
                                                        (Integer/parseInt Ld)
                                                        (Integer/parseInt R)))
  (POST "/setup/remove-unit" [] (swap! hexenhammer-state cs/remove-unit))
  (POST "/setup/swap-terrain" [terrain] (swap! hexenhammer-state cs/swap-terrain (keyword terrain)))

  (GET "/movement/switch-movement/:movement" [movement] (swap! hexenhammer-state cm/switch-movement (keyword movement)))
  (POST "/movement/skip-movement" [] (swap! hexenhammer-state cm/skip-movement))
  (POST "/movement/finish-movement" [] (swap! hexenhammer-state cm/finish-movement)))


(defroutes select-handler
  (GET "/select/setup/select-hex" [cube] (swap! hexenhammer-state cs/select-hex cube))
  (GET "/select/setup/add-unit" [cube] (swap! hexenhammer-state cs/select-add-unit cube))
  (GET "/select/setup/remove-unit" [cube] (swap! hexenhammer-state cs/select-remove-unit cube))
  (GET "/select/movement/select-hex" [cube] (swap! hexenhammer-state cm/select-hex cube))
  (GET "/select/movement/reform" [cube] (swap! hexenhammer-state cm/select-reform cube))
  (GET "/select/movement/forward" [cube] (swap! hexenhammer-state cm/select-forward cube))
  (GET "/select/movement/reposition" [cube] (swap! hexenhammer-state cm/select-reposition cube))
  (GET "/select/movement/march" [cube] (swap! hexenhammer-state cm/select-march cube)))


(defroutes move-handler
  (GET "/move/movement/reform" [pointer] (swap! hexenhammer-state cm/move-reform pointer))
  (GET "/move/movement/forward" [pointer] (swap! hexenhammer-state cm/move-forward pointer))
  (GET "/move/movement/reposition" [pointer] (swap! hexenhammer-state cm/move-reposition pointer))
  (GET "/move/movement/march" [pointer] (swap! hexenhammer-state cm/move-march pointer)))


(def hexenhammer-app
  (-> (routes view-handler
              (-> controller-handler
                  (wrap-routes ws/wrap-redirect-phase)
                  (wrap-routes wrap-params))
              (-> select-handler
                  (wrap-routes ws/wrap-redirect-phase)
                  (wrap-routes ws/wrap-select)
                  (wrap-routes wrap-params))
              (-> move-handler
                  (wrap-routes ws/wrap-redirect-phase)
                  (wrap-routes ws/wrap-move)
                  (wrap-routes wrap-params)))))


;; (defonce server (run-jetty #'hexenhammer-app {:port 8080 :join? false}))
;; (.start server)
;; (.stop server)
