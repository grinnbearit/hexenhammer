(ns hexenhammer.logic.entity.mover)


(defn gen-mover
  "Returns a mover entity, options is a set of possible facings,
  selected and highlighted are single facings"
  [player
   & {:keys [options selected highlighted presentation]
      :or {options #{} presentation :future}}]
  {:entity/class :mover
   :entity/presentation :default

   :unit/player player

   :mover/options options
   :mover/selected selected
   :mover/highlighted highlighted
   :mover/presentation presentation})
