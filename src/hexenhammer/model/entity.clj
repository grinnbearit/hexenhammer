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
   :unit/facing facing
   :unit/M 8})


(defn gen-mover
  "Returns a mover entity, options is a set of possible facings,
  selected and highlighted are single facings"
  [cube player & {:keys [presentation interaction options selected highlighted]
                  :or {presentation
                       :default interaction
                       :default options #{}}}]
  {:entity/class :mover
   :entity/cube cube
   :entity/presentation presentation
   :entity/interaction interaction
   :unit/player player
   :mover/options options
   :mover/selected selected
   :mover/highlighted highlighted})


(defn gen-shadow
  "Returns a shadow entity, a stripped down unit"
  [cube player facing]
  {:entity/class :unit
   :entity/cube cube
   :unit/player player
   :unit/facing facing})
