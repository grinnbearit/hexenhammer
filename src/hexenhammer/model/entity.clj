(ns hexenhammer.model.entity)


(defn gen-open-ground
  "Returns an open ground terrain object"
  [cube]
  {:entity/class :terrain
   :entity/name "terrain"
   :entity/cube cube
   :entity/state :default
   :entity/los 0

   :terrain/type :open})


(defn gen-dangerous-terrain
  "Returns a dangerous terrain object"
  [cube]
  (assoc (gen-open-ground cube)
         :terrain/type :dangerous))


(defn gen-impassable-terrain
  "Returns an impassable terrain object"
  [cube]
  (assoc (gen-open-ground cube)
         :terrain/type :impassable
         :entity/los 5))


(defn gen-infantry
  "Returns a generic infantry entity"
  [cube player id facing
   & {:keys [M Ld]
      :or {M 4 Ld 7}}]
  {:entity/class :unit
   :entity/name "infantry"
   :entity/cube cube
   :entity/state :default
   :entity/los 1

   :unit/player player
   :unit/id id
   :unit/facing facing
   :unit/M M
   :unit/Ld Ld
   :unit/W 1
   :unit/F 4
   :unit/R 4
   :unit/model-strength 1
   :unit/ranks 4
   :unit/damage 0})


(defn gen-mover
  "Returns a mover entity, options is a set of possible facings,
  selected and highlighted are single facings"
  [cube player
   & {:keys [options selected highlighted state]
      :or {options #{} state :future}}]
  {:entity/class :mover
   :entity/cube cube
   :entity/state :default

   :unit/player player

   :mover/options options
   :mover/selected selected
   :mover/highlighted highlighted
   :mover/state state})


(defn gen-shadow
  "Returns a shadow entity, a stripped down unit"
  [cube player facing]
  {:entity/class :unit
   :entity/cube cube

   :unit/player player
   :unit/facing facing})
