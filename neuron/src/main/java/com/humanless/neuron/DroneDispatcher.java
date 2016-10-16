package com.humanless.neuron;

import java.util.ArrayList;

/**
 * Base drone dispatcher. Dispatcher is responsible for listening to drone events and state changes
 * as well as maintaining the current states of the drone.
 */
public class DroneDispatcher {
    private ArrayList<DroneListener> listeners;
    private DroneState droneState;

    /**
     * Add listener to dispatcher.
     *
     * @param listener The listener to add.
     */
    public void addListener(DroneListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener The listener to remove.
     */
    public void removeListener(DroneListener listener) {
        listeners.remove(listener);
    }

    /**
     * Get the current drone state.
     *
     * @return The current drone state.
     */
    public DroneState getDroneState() {
        return droneState;
    }

    /**
     * Set the current drone state.
     *
     * @param droneState The drone state to set.
     */
    protected void setDroneState(DroneState droneState) {
        this.droneState = droneState;
    }

    /**
     * Notify all listener listening for the specified event. The callback will triggered if the
     * requirement states are satisfied.
     *
     * @param event The event to dispatch.
     */
    protected void dispatch(int event) {
        for (DroneListener listener : listeners) {
            if (listener.isForEvent(event) && droneState.isInStates(listener.getStates())) {
                listener.getCallback().run();
            }
        }
    }
}
