(ns hexenhammer.model.entity)


(defn gen-terrain
  "Returns a generic terrain entity"
  [cube]
  {:hexenhammer/entity :terrain
   :terrain/cube cube})
