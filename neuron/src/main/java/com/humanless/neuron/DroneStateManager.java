package com.humanless.neuron;

import android.util.SparseArray;

import java.util.Date;

/**
 * Base drone state manager. Drone state manager is responsible for maintaining the current state
 * of the drone.
 */
public abstract class DroneStateManager {
    private SparseArray<Object> states = new SparseArray<>();
    private SparseArray<Date> updateTimes = new SparseArray<>();

    /**
     * Set the value of the specified state. State will not be set if the value is the same as
     * existing value.
     *
     * @param state The state to set.
     * @param value The value.
     * @return true, if state is updated; otherwise, false.
     */
    public boolean setState(int state, Object value) {
        return setStates(state, value, false);
    }

    /**
     * Set the value of the specified state. State will not be set if the value is the same as
     * existing value, unless force is true.
     *
     * @param state The state to set.
     * @param value The value.
     * @param force If true, the value will be set reguardless of existing value; otherwise, it will
     *              only set if the value is different from existing value.
     * @return true, if state is updated; otherwise, false.
     */
    public boolean setStates(int state, Object value, boolean force) {
        Object currentValue = getState(state);

        if (currentValue != value || force) {
            states.setValueAt(state, value);
            updateTimes.setValueAt(state, new Date());
            return true;
        }

        return false;
    }

    /**
     * Get the current value of the specified state.
     *
     * @param state The state to retrieve.
     * @return The existing value of the state. Value can be null.
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
