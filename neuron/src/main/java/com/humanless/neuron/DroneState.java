package com.humanless.neuron;

/**
 * Base drone state.
 */
public abstract class DroneState {
    /**
     * Check if the drone satisfies the specified list of states.
     *
     * @param states The list of states to check.
     * @return true, if the drone's current state matches the specified states; otherwise, false.
     */
    public abstract boolean isInStates(int[] states);
}
