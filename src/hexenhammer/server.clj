(ns hexenhammer.server
  (:require [ring.util.response :refer [redirect]]))


(defn wrap-redirect-home
  "Executes a handler with side effects and then redirects to home"
  [handler]
  (fn [request]
    (do (handler request)
        (redirect "/"))))
