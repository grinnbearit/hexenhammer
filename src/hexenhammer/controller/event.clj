(ns hexenhammer.controller.event)


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
  [state event]
  (assoc state :game/phase [:event :dangerous]))
