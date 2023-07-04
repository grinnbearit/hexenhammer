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
  (render/render-state @hexenhammer-state))


(defn select-handler
  [{params :query-params}]
  (let [{:strs [q r s]} params]
    (if q
      (let [cube (cube/->Cube (Integer/parseInt q)
                              (Integer/parseInt r)
                              (Integer/parseInt s))]
        (do (swap! hexenhammer-state transition/select-cube cube)
            (redirect "/")))
      (do (swap! hexenhammer-state transition/unselect-cube)
          (redirect "/")))))


(defn add-unit-handler
  [{{:strs [q r s player facing]} :params}]
  (let [cube (cube/->Cube (Integer/parseInt q)
                          (Integer/parseInt r)
                          (Integer/parseInt s))]
    (do (swap! hexenhammer-state transition/add-unit cube
               :player (Integer/parseInt player)
               :facing (keyword facing))
        (redirect "/"))))


(defn remove-unit-handler
  [{{:strs [q r s player facing]} :params}]
  (let [cube (cube/->Cube (Integer/parseInt q)
                          (Integer/parseInt r)
                          (Integer/parseInt s))]
    (do (swap! hexenhammer-state transition/remove-unit cube)
        (redirect "/"))))


(defn to-movement-handler
  [request]
  (do (swap! hexenhammer-state transition/to-movement)
      (redirect "/")))


(defroutes app-handler
  (GET "/" [] default-handler)
  (GET "/select" [] select-handler)
  (POST "/setup/add-unit" [] add-unit-handler)
  (POST "/setup/remove-unit" [] remove-unit-handler)
  (POST "/setup/to-movement" [] to-movement-handler)
  (route/not-found "oops"))


(def app
  (-> app-handler
      wrap-params))


;; (defonce server (run-jetty #'app {:port 8080 :join? false}))
;; (.start server)
;; (.stop server)
