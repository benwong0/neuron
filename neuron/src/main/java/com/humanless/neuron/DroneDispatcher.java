package com.humanless.neuron;

import java.util.ArrayList;
import java.util.List;

/**
 * Base drone dispatcher. Dispatcher is responsible for listening to drone events and state changes
 * as well as maintaining the current states of the drone via DroneStateManager.
 */
public abstract class DroneDispatcher<EventType, StateType> {
    private final ArrayList<DroneListener<EventType, StateType>> listeners = new ArrayList<>();
    private DroneStateManager<StateType> droneStateManager = new DroneStateManager<>();

    /**
     * Add listener to dispatcher.
     *
     * @param listener The listener to add.
     */
    public void addListener(DroneListener<EventType, StateType> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener.
     *
     * @param listener The listener to remove.
     */
    public void removeListener(DroneListener<EventType, StateType> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Get the drone state manager.
     *
     * @return The drone state manager.
     */
    public DroneStateManager<StateType> getDroneStateManager() {
        return droneStateManager;
    }

    /**
     * Notify all listener listening for the specified event. The callback will triggered if the
     * requirement states are satisfied.
     *
     * @param event The event to dispatch.
     */
    protected void dispatch(EventType event) {
        dispatch(event, null);
    }

    /**
     * Notify all listener listening for the specified event. The callback will triggered if the
     * requirement states are satisfied.
     *
     * @param event      The event to dispatch.
     * @param parameters A list of parameters containing results from the listener.
     *                   Parameters can be null.
     */
    protected void dispatch(EventType event, List<Object> parameters) {
        synchronized (listeners) {
            for (DroneListener<EventType, StateType> listener : listeners) {
                if (!listener.isForEvent(event) || !droneStateManager.isInStates(listener.getStates())) {
                    continue;
                }

                DroneListenerCallback callback = listener.getCallback();
                if (callback != null) {
                    callback.run(parameters);
                }
            }
        }
    }
}
