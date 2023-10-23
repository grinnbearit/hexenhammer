(ns hexenhammer.logic.entity.terrain)


(defn gen-open-ground
  "Returns an open ground terrain object"
  []
  {:entity/class :terrain
   :entity/presentation :default
   :entity/los 0
   :terrain/type :open})
