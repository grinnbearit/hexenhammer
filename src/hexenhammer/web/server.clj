(ns hexenhammer.web.server
  (:require [hexenhammer.logic.cube :as lc]
            [ring.util.response :refer [redirect]]
            [hexenhammer.render.svg :refer [phase->url]]))


(defn wrap-redirect-phase
  "Executes a handler with side effects and then redirects to home"
  [handler]
  (fn [request]
    (let [{:keys [game/phase]} (handler request)]
      (redirect (phase->url "/" phase)))))


(defn wrap-select
  [handler]
  (fn [{{:strs [q r s]} :params :as request}]
    (let [cube (lc/->Cube (Integer/parseInt q)
                          (Integer/parseInt r)
                          (Integer/parseInt s))]
      (handler (assoc-in request [:params "cube"] cube)))))
