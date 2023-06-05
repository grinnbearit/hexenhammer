(ns hexenhammer.core
  (:require [hexenhammer.render.core :as render]
            [hexenhammer.transition :as transition]
            [hexenhammer.cube :as cube]
            [ring.util.response :refer [redirect]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]))


(def hexenhammer-state (atom (transition/gen-initial-state 8 12)))


(defn default-handler
  [request]
  (do (swap! hexenhammer-state transition/unselect-cube)
      (render/render-state @hexenhammer-state)))


(defn select-handler
  [{params :query-params}]
  (let [{:strs [q r s]} params]
    (if q
      (let [cube (cube/->Cube (Integer/parseInt q)
                              (Integer/parseInt r)
                              (Integer/parseInt s))]
        (do (swap! hexenhammer-state transition/select-cube cube)
            (render/render-state @hexenhammer-state)))
      (do (swap! hexenhammer-state transition/unselect-cube)
          (render/render-state @hexenhammer-state)))))


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


(defn add-unit-handler
  [{{:strs [q r s player facing]} :params}]
  (let [cube (cube/->Cube (Integer/parseInt q)
                          (Integer/parseInt r)
                          (Integer/parseInt s))]
    (do (swap! hexenhammer-state transition/add-unit cube
               :player (Integer/parseInt player)
               :facing (keyword facing))
        (redirect "/modify"))))


(defn remove-unit-handler
  [{{:strs [q r s player facing]} :params}]
  (let [cube (cube/->Cube (Integer/parseInt q)
                          (Integer/parseInt r)
                          (Integer/parseInt s))]
    (do (swap! hexenhammer-state transition/remove-unit cube)
        (redirect "/modify"))))


(defroutes app-handler
  (GET "/" [] default-handler)
  (GET "/select" [] select-handler)
  (GET "/modify" [] modify-handler)
  (POST "/modify/add-unit" [] add-unit-handler)
  (POST "/modify/remove-unit" [] remove-unit-handler))


(def app
  (-> app-handler
      wrap-params))


;; (defonce server (run-jetty #'app {:port 8080 :join? false}))
;; (.start server)
;; (.stop server)
