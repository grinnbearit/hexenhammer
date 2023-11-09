(ns hexenhammer.controller.event
  (:require [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement.flee :as lbmf]
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


(defmethod trigger-event :dangerous-terrain
  [{:keys [game/units] :as state} {:keys [event/cube event/unit-key]}]
  (if-let [unit-cube (tu/get-unit units unit-key)]
    (let [unit (get-in state [:game/battlefield unit-cube])
          models (leu/models unit)
          roll (td/roll! models)
          models-destroyed (td/matches roll 1)
          unit-destroyed? (<= models models-destroyed)]
      (-> (if unit-destroyed?
            (tsu/destroy-unit state unit-cube)
            (tsu/destroy-models state unit-cube cube models-destroyed))
          (assoc :game/phase [:event :dangerous-terrain])
          (update :game/event assoc
                  :unit unit
                  :models-destroyed models-destroyed
                  :unit-destroyed? unit-destroyed?
                  :roll roll)
          (tsb/reset-battlemap [cube unit-cube])
          (update :game/battlemap tb/set-presentation :marked)))
    (trigger state)))


(defmethod trigger-event :heavy-casualties
  [{:keys [game/units game/battlefield] :as state} {:keys [event/cube event/unit-key]}]
  (if-let [unit-cube (tu/get-unit units unit-key)]
    (if-let [panickable? (lbu/panickable? battlefield unit-cube)]
      (let [unit (battlefield unit-cube)
            roll (td/roll! 2)
            passed? (<= (apply + roll) (:unit/Ld unit))]
        (-> (if passed?
              (assoc state :game/phase [:event :heavy-casualties :passed])
              (assoc state :game/phase [:event :heavy-casualties :failed]))
            (update-in [:game/battlefield unit-cube] leu/set-panicked)
            (update :game/event assoc
                    :source-cube cube
                    :unit-cube unit-cube
                    :roll roll)
            (tsb/reset-battlemap [cube unit-cube])
            (update :game/battlemap tb/set-presentation :marked)))
      (trigger state))
    (trigger state)))


(defn flee-heavy-casualties
  [{:keys [game/battlefield] :as state}]
  (let [{:keys [unit-cube source-cube]} (:game/event state)
        unit (get-in state [:game/battlefield unit-cube])
        roll (td/roll! 2)
        {:keys [end cube->tweeners edge?]} (lbmf/flee battlefield
                                                      unit-cube
                                                      source-cube
                                                      (apply + roll))]
    (-> (if edge?
          (tsu/escape-unit state unit-cube (:cube end))
          (-> (update-in state [:game/battlefield unit-cube] leu/set-flee)
              (tsu/move-unit unit-cube end)))
        (assoc :game/phase [:event :heavy-casualties :flee])
        (update :game/event assoc
                :edge? edge?
                :unit unit
                :roll roll)
        (tsb/reset-battlemap [source-cube (:cube end)])
        (update :game/battlemap merge cube->tweeners)
        (update :game/battlemap tb/set-presentation [source-cube (:cube end)] :marked))))
