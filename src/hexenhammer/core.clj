(ns hexenhammer.core
  (:require [compojure.core :refer [defroutes routes GET POST wrap-routes]]
            [hexenhammer.transition.core :as t]
            [hexenhammer.controller.core :as c]
            [hexenhammer.controller.setup :as cs]
            [hexenhammer.controller.event :as ce]
            [hexenhammer.controller.movement :as cm]
            [hexenhammer.controller.charge.core :as cc]
            [hexenhammer.controller.charge.reaction :as ccr]
            [hexenhammer.view.core :as v]
            [hexenhammer.view.setup :as vs]
            [hexenhammer.view.event :as ve]
            [hexenhammer.view.movement :as vm]
            [hexenhammer.view.close-combat :as vo]
            [hexenhammer.view.charge.core :as vc]
            [hexenhammer.view.charge.reaction :as vcr]
            [hexenhammer.web.server :as ws]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]))


(def hexenhammer-state (atom (t/gen-initial-state 8 12)))


(defroutes view-handler
  (GET "/" [] (v/select-hex @hexenhammer-state))
  (GET "/favicon.ico" [] "")
  (GET "/setup/select-hex" [] (vs/select-hex @hexenhammer-state))
  (GET "/setup/add-unit" [] (vs/add-unit @hexenhammer-state))
  (GET "/setup/remove-unit" [] (vs/remove-unit @hexenhammer-state))

  (GET "/charge/select-hex" [] (vc/select-hex @hexenhammer-state))
  (GET "/charge/pick-targets" [] (vc/pick-targets @hexenhammer-state))
  (GET "/charge/declare-targets" [] (vc/declare-targets @hexenhammer-state))
  (GET "/charge/to-movement" [] (vc/to-movement @hexenhammer-state))

  (GET "/charge/reaction/select-hex" [] (vcr/select-hex @hexenhammer-state))
  (GET "/charge/reaction/hold" [] (vcr/hold @hexenhammer-state))
  (GET "/charge/reaction/fled" [] (vcr/fled @hexenhammer-state))
  (GET "/charge/reaction/flee" [] (vcr/flee @hexenhammer-state))
  (GET "/charge/reaction/fleeing" [] (vcr/fleeing @hexenhammer-state))

  (GET "/charge/reaction/flee/roll" [] (vcr/flee-roll @hexenhammer-state))
  (GET "/charge/reaction/finish-reaction" [] (vcr/finish-reaction @hexenhammer-state))

  (GET "/movement/select-hex" [] (vm/select-hex @hexenhammer-state))
  (GET "/movement/reform" [] (vm/reform @hexenhammer-state))
  (GET "/movement/forward" [] (vm/forward @hexenhammer-state))
  (GET "/movement/reposition" [] (vm/reposition @hexenhammer-state))
  (GET "/movement/march" [] (vm/march @hexenhammer-state))
  (GET "/movement/to-close-combat" [] (vm/to-close-combat @hexenhammer-state))

  (GET "/event/dangerous-terrain" [] (ve/dangerous-terrain @hexenhammer-state))
  (GET "/event/heavy-casualties/passed" [] (ve/heavy-casualties-passed @hexenhammer-state))
  (GET "/event/heavy-casualties/failed" [] (ve/heavy-casualties-failed @hexenhammer-state))
  (GET "/event/heavy-casualties/flee" [] (ve/heavy-casualties-flee @hexenhammer-state))
  (GET "/event/panic/passed" [] (ve/panic-passed @hexenhammer-state))
  (GET "/event/panic/failed" [] (ve/panic-failed @hexenhammer-state))
  (GET "/event/panic/flee" [] (ve/panic-flee @hexenhammer-state))
  (GET "/event/opportunity-attack" [] (ve/opportunity-attack @hexenhammer-state))

  (GET "/close-combat" [] (vo/close-combat @hexenhammer-state)))


(defroutes controller-handler
  (POST "/to-setup" [] (swap! hexenhammer-state c/to-setup))
  (POST "/to-start" [] (swap! hexenhammer-state c/to-start))
  (POST "/to-movement" [] (swap! hexenhammer-state c/to-movement))
  (POST "/to-close-combat" [] (swap! hexenhammer-state c/to-close-combat))
  (POST "/setup/add-unit" [player facing M Ld R] (swap! hexenhammer-state cs/add-unit
                                                        (Integer/parseInt player)
                                                        (keyword facing)
                                                        (Integer/parseInt M)
                                                        (Integer/parseInt Ld)
                                                        (Integer/parseInt R)))
  (POST "/setup/remove-unit" [] (swap! hexenhammer-state cs/remove-unit))
  (POST "/setup/swap-terrain" [terrain] (swap! hexenhammer-state cs/swap-terrain (keyword terrain)))

  (POST "/charge/declare-targets" [] (swap! hexenhammer-state cc/declare-targets))
  (POST "/charge/skip-charge" [] (swap! hexenhammer-state cc/skip-charge))
  (POST "/charge/finish-reaction" [] (swap! hexenhammer-state cc/finish-reaction))

  (GET "/charge/reaction/switch-reaction/:reaction" [reaction] (swap! hexenhammer-state ccr/switch-reaction (keyword reaction)))
  (POST "/charge/reaction/hold" [] (swap! hexenhammer-state ccr/hold))
  (POST "/charge/reaction/fled" [] (swap! hexenhammer-state ccr/fled))
  (POST "/charge/reaction/flee" [] (swap! hexenhammer-state ccr/flee))
  (POST "/charge/reaction/fleeing" [] (swap! hexenhammer-state ccr/fleeing))
  (POST "/charge/reaction/trigger" [] (swap! hexenhammer-state ccr/trigger))

  (GET "/movement/switch-movement/:movement" [movement] (swap! hexenhammer-state cm/switch-movement (keyword movement)))
  (POST "/movement/skip-movement" [] (swap! hexenhammer-state cm/skip-movement))
  (POST "/movement/finish-movement" [] (swap! hexenhammer-state cm/finish-movement))
  (POST "/movement/test-leadership" [] (swap! hexenhammer-state cm/test-leadership))

  (POST "/event/trigger" [] (swap! hexenhammer-state ce/trigger))
  (POST "/event/heavy-casualties/flee" [] (swap! hexenhammer-state ce/flee-heavy-casualties))
  (POST "/event/panic/flee" [] (swap! hexenhammer-state ce/flee-panic)))


(defroutes select-handler
  (GET "/select/setup/select-hex" [cube] (swap! hexenhammer-state cs/select-hex cube))
  (GET "/select/setup/add-unit" [cube] (swap! hexenhammer-state cs/select-add-unit cube))
  (GET "/select/setup/remove-unit" [cube] (swap! hexenhammer-state cs/select-remove-unit cube))

  (GET "/select/charge/select-hex" [cube] (swap! hexenhammer-state cc/select-hex cube))
  (GET "/select/charge/pick-targets" [cube] (swap! hexenhammer-state cc/select-pick-targets cube))
  (GET "/select/charge/declare-targets" [cube] (swap! hexenhammer-state cc/select-declare-targets cube))

  (GET "/select/charge/reaction/select-hex" [cube] (swap! hexenhammer-state ccr/select-hex cube))
  (GET "/select/charge/reaction/hold" [cube] (swap! hexenhammer-state ccr/select-hold cube))
  (GET "/select/charge/reaction/flee" [cube] (swap! hexenhammer-state ccr/select-flee cube))

  (GET "/select/movement/select-hex" [cube] (swap! hexenhammer-state cm/select-hex cube))
  (GET "/select/movement/reform" [cube] (swap! hexenhammer-state cm/select-reform cube))
  (GET "/select/movement/forward" [cube] (swap! hexenhammer-state cm/select-forward cube))
  (GET "/select/movement/reposition" [cube] (swap! hexenhammer-state cm/select-reposition cube))
  (GET "/select/movement/march" [cube] (swap! hexenhammer-state cm/select-march cube)))


(defroutes move-handler
  (GET "/move/charge/pick-targets" [pointer] (swap! hexenhammer-state cc/move-pick-targets pointer))
  (GET "/move/charge/declare-targets" [pointer] (swap! hexenhammer-state cc/move-declare-targets pointer))

  (GET "/move/movement/reform" [pointer] (swap! hexenhammer-state cm/move-reform pointer))
  (GET "/move/movement/forward" [pointer] (swap! hexenhammer-state cm/move-forward pointer))
  (GET "/move/movement/reposition" [pointer] (swap! hexenhammer-state cm/move-reposition pointer))
  (GET "/move/movement/march" [pointer] (swap! hexenhammer-state cm/move-march pointer)))


(def hexenhammer-app
  (-> (routes (-> view-handler
                  (wrap-routes (ws/wrap-current-phase hexenhammer-state)))
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
