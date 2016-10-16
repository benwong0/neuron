package com.humanless.neuron;

import android.support.annotation.NonNull;

/**
 * Base drone listener.
 */
public class DroneListener {
    private int[] events;
    private int[] states;
    private Runnable callback;

    /**
     * Instantiate a listener for the specified events and state. The callback will be triggered
     * on any of the events as long as the states are satisfied.
     *
     * @param events   A list of the events to listen for.
     * @param states   A list of the states the drone must be in in order to trigger the callback.
     *                 Value can be null.
     * @param callback The callback to trigger.
     */
    public DroneListener(@NonNull int[] events, int[] states, @NonNull Runnable callback) {
        this.events = events;
        this.states = states;
        this.callback = callback;
    }

    /**
     * Get the list of events it's listening to.
     *
     * @return An int array representing the list of events.
     */
    public int[] getEvents() {
        return events;
    }

    /**
     * Get the list of states the drone must be in in order to trigger the callback.
     *
     * @return An int array representing the list of states. Value can be null.
     */
    public int[] getStates() {
        return states;
    }

    /**
     * Get the callback.
     *
     * @return The event callback.
     */
    public Runnable getCallback() {
        return callback;
    }

    /**
     * Check if this listener is for a specific event.
     *
     * @param event The event to check for.
     * @return true, if this listener will trigger on the specified event; otherwise, false.
     */
    public boolean isForEvent(int event) {
        for (int e : events) {
            if (e == event) {
                return true;
            }
        }
        return false;
    }
}
