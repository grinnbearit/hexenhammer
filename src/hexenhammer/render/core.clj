(ns hexenhammer.render.core
  (:require [hexenhammer.render.entity :as re]
            [hexenhammer.render.svg :as rs]))


(defn render-battlefield
  [{:keys [game/setup game/battlefield]}]
  (let [{:keys [rows columns]} setup]
    [:svg (rs/size->dim rows columns)
     (for [[cube entity] battlefield]
       (re/render entity cube))]))
