(ns hexenhammer.controller.core)


(defmulti select (fn [state cube] (:game/phase state)))


(defmethod select :setup
  [{:keys [game/battlefield] :as state} cube]
  (let [terrain (battlefield cube)]
    (->> (case (:entity/presentation terrain)
           :selected (assoc terrain :entity/presentation :default)
           :default (assoc terrain :entity/presentation :selected))
         (assoc-in state [:game/battlefield cube]))))
