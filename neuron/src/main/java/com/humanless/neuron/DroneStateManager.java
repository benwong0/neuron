package com.humanless.neuron;

import android.util.SparseArray;

/**
 * Base drone state manager. Drone state manager is responsible for maintaining the current state
 * of the drone.
 */
public abstract class DroneStateManager {
    private SparseArray<Object> states = new SparseArray<>();

    /**
     * Set the value of the specified state.
     *
     * @param state The state to set.
     * @param value The value.
     */
    public void setState(int state, Object value) {
        states.setValueAt(state, value);
    }

    /**
     * Get the current value of the specified state.
     *
     * @param state The state to retrieve.
     * @return The value.
     */
    public Object getState(int state) {
        return states.get(state);
    }

    /**
     * Check if the drone currently matches the specified state.
     *
     * @param state The state to check.
     * @param value The value the state should be in.
     * @return true, if the drone's current state matches the specified state; otherwise, false.
     */
    public boolean isInState(int state, Object value) {
        if (getState(state) == value) {
            return true;
        }
        return false;
    }

    /**
     * Check if the drone satisfies the specified list of states.
     *
     * @param states The list of states to check.
     * @return true, if the drone's current state matches the specified states; otherwise, false.
     */
    public boolean isInStates(SparseArray<Object> states) {
        for (int i = 0; i < states.size(); i++) {
            int key = states.keyAt(i);
            Object value = states.get(key);
            if (getState(key) != value) {
                return false;
            }
        }
        return true;
    }
}
