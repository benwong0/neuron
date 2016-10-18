package com.humanless.neuron.dji;

import com.humanless.neuron.DroneStateManager;

/**
 * Drone state manager for DJI drones.
 */
public class DjiDroneStateManager extends DroneStateManager {
    /**
     * Set the value of the specified state.
     *
     * @param state The state to set.
     * @param value The value.
     * @return true, if state is updated; otherwise, false.
     */
    public boolean setState(DjiDroneState state, Object value) {
        return setState(state.ordinal(), value);
    }

    /**
     * Get the current value of the specified state.
     *
     * @param state The state to retrieve.
     * @return The value.
     */
    public Object getState(DjiDroneState state) {
        return getState(state.ordinal());
    }

    /**
     * Check if the drone currently matches the specified state.
     *
     * @param state The state to check.
     * @param value The value the state should be in.
     * @return true, if the drone's current state matches the specified state; otherwise, false.
     */
    public boolean isInState(DjiDroneState state, Object value) {
        return isInState(state.ordinal(), value);
    }
}
