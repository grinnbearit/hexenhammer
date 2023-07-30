(ns hexenhammer.model.entity)


(defn gen-terrain
  "Returns a generic terrain entity"
  [cube & {:keys [presentation interaction] :or {presentation :default interaction :default}}]
  {:entity/class :terrain
   :entity/name "terrain"
   :entity/cube cube
   :entity/presentation presentation
   :entity/interaction interaction})


(defn gen-unit
  "Returns a generic unit entity"
  [cube player id facing & {:keys [presentation interaction] :or {presentation :default interaction :default}}]
  {:entity/class :unit
   :entity/name "unit"
   :entity/cube cube
   :entity/presentation presentation
   :entity/interaction interaction
   :unit/player player
   :unit/id id
   :unit/facing facing})


(defn gen-mover
  "Returns a mover entity all 6 facings with 1 selected"
  [cube player facing & {:keys [presentation interaction]
                         :or {presentation :default interaction :default}}]
  {:entity/class :mover
   :entity/cube cube
   :entity/presentation presentation
   :entity/interaction interaction
   :unit/player player
   :unit/facing facing})
