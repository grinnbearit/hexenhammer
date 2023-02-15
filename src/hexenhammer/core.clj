(ns hexenhammer.core
  (:require [hexenhammer.render.core :as render]
            [hexenhammer.transition :as transition]
            [hexenhammer.cube :as cube]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]))


(def hexenhammer-state (atom (transition/gen-initial-state 8 12)))


(defn default-handler
  [request]
  (render/render-state @hexenhammer-state))


(defn modify-handler
  [{params :query-params}]
  (let [{:strs [q r s]} params]
    (if q
      (let [cube (cube/->Cube (Integer/parseInt q)
                              (Integer/parseInt r)
                              (Integer/parseInt s))]
        (do (swap! hexenhammer-state transition/select-cube cube)
            (render/render-modify @hexenhammer-state)))
      (do (swap! hexenhammer-state transition/unselect-cube)
          (render/render-modify @hexenhammer-state)))))


(defroutes app-handler
  (GET "/" [] default-handler)
  (GET "/modify" [] modify-handler))


(def app
  (-> app-handler
      wrap-params))


(defonce server (run-jetty #'app {:port 8080 :join? false}))
;; (.start server)
;; (.stop server)
