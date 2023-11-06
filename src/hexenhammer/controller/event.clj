(ns hexenhammer.controller.event
  (:require [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.transition.dice :as td]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.units :as tsu]
            [hexenhammer.transition.state.battlemap :as tsb]))


(defmulti trigger-event (fn [_ event] (:event/type event)))


(defn trigger
  "pops the next event in the queue, if empty triggers the stored callback"
  ([state]
   (if-let [event (peek (:game/events state))]
     (-> (update state :game/events pop)
         (update :game/event select-keys [:callback])
         (dissoc :game/battlemap)
         (trigger-event event))
     (let [callback (get-in state [:game/event :callback])]
       (-> (dissoc state :game/battlemap :game/event)
           (callback)))))
  ([state callback]
   (-> (assoc-in state [:game/event :callback] callback)
       (trigger))))


(defmethod trigger-event :dangerous
  [{:keys [game/units] :as state} {:keys [event/cube event/unit-key]}]
  (if-let [unit-cube (tu/get-unit units unit-key)]
    (let [unit (get-in state [:game/battlefield unit-cube])
          models (leu/models unit)
          roll (td/roll! models)
          models-destroyed (td/matches roll 1)
          unit-destroyed? (<= models models-destroyed)]
      (-> (if unit-destroyed?
            (tsu/destroy-unit state unit-cube)
            (tsu/destroy-models state unit-cube models-destroyed))
          (assoc :game/phase [:event :dangerous])
          (update :game/event assoc
                  :unit unit
                  :models-destroyed models-destroyed
                  :unit-destroyed? unit-destroyed?
                  :roll roll)
          (tsb/reset-battlemap [cube unit-cube])
          (update :game/battlemap tb/set-presentation :marked)))
    (trigger state)))
