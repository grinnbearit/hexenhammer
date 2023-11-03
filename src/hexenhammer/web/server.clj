(ns hexenhammer.web.server
  (:require [hexenhammer.logic.cube :as lc]
            [ring.util.response :refer [redirect]]
            [hexenhammer.render.bit :refer [phase->url]]))


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


(defn wrap-move
  [handler]
  (fn [{{:strs [facing q r s]} :params :as request}]
    (let [cube (lc/->Cube (Integer/parseInt q)
                          (Integer/parseInt r)
                          (Integer/parseInt s))
          pointer (lc/->Pointer cube (keyword facing))]
      (handler (assoc-in request [:params "pointer"] pointer)))))


(defn wrap-current-phase
  "Returns a handler that redirects to the page determined by the current phase if the phase doesn't match the url"
  [state!]
  (fn [handler]
    (fn [{:keys [compojure/route] :as request}]
      (let [url (route 1)
            phase-url (phase->url "/" (:game/phase @state!))]
        (if (= phase-url url)
          (handler request)
          (redirect phase-url))))))
