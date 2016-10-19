package com.humanless.neuron;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Base drone state manager. Drone state manager is responsible for maintaining the current state
 * of the drone.
 */
public class DroneStateManager<StateType> {
    private HashMap<StateType, Object> states = new HashMap<>();
    private HashMap<StateType, Date> updateTimes = new HashMap<>();

    /**
     * Clear all states.
     */
    public void resetState() {
        states.clear();
        updateTimes.clear();
    }

    /**
     * Set the value of the specified state. State will not be set if the value is the same as
     * existing value.
     *
     * @param state The state to set.
     * @param value The value.
     * @return true, if state is updated; otherwise, false.
     */
    public boolean setState(StateType state, Object value) {
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
    public boolean setStates(StateType state, Object value, boolean force) {
        Object currentValue = getState(state);

        if (currentValue != value || force) {
            states.put(state, value);
            updateTimes.put(state, new Date());
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
    public Object getState(StateType state) {
        return states.get(state);
    }

    /**
     * Check if the drone currently matches the specified state.
     *
     * @param state The state to check.
     * @param value The value the state should be in.
     * @return true, if the drone's current state matches the specified state; otherwise, false.
     */
    public boolean isInState(StateType state, Object value) {
        return (getState(state) == value);
    }

    /**
     * Check if the drone's specified state equals the value for at least the specified number of
     * seconds.
     *
     * @param state        The state to check.
     * @param value        The value the state should be in.
     * @param milliseconds Minimum time since state changed to the specified value.
     * @return true, if the drone's current state matches the specified state for specified
     * duration; otherwise, false.
     */
    public boolean isInState(StateType state, Object value, long milliseconds) {
        Date lastUpdate = updateTimes.get(state);
        if (lastUpdate != null) {
            long update = lastUpdate.getTime();
            long now = new Date().getTime();
            long timeDiff = now - update;

            if (timeDiff > milliseconds) {
                return isInState(state, value);
            }
        }
        return false;
    }

    /**
     * Check if the drone satisfies the specified list of states.
     *
     * @param states The list of states to check.
     * @return true, if the drone's current state matches the specified states; otherwise, false.
     */
    public boolean isInStates(HashMap<StateType, Object> states) {
        for (Map.Entry<StateType, Object> entry : states.entrySet()) {
            if (getState(entry.getKey()) != entry.getValue()) {
                return false;
            }
        }
        return true;
    }
}
