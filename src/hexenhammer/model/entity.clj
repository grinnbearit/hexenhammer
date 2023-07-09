(ns hexenhammer.model.entity)


(defn gen-terrain
  "Returns a generic terrain entity"
  [cube & {:keys [presentation interaction] :or {presentation :default interaction :default}}]
  {:entity/name :terrain
   :entity/cube cube
   :entity/presentation presentation
   :entity/interaction interaction})
