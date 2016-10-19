package com.humanless.neuron;

import android.support.annotation.NonNull;

import java.util.HashMap;

/**
 * Base drone listener.
 */
public class DroneListener<EventType, StateType> {
    private EventType[] events;
    private HashMap<StateType, Object> states;
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
    public DroneListener(@NonNull EventType[] events, HashMap<StateType, Object> states, @NonNull Runnable callback) {
        this.events = events;
        this.states = states;
        this.callback = callback;
    }

    /**
     * Get the list of events it's listening to.
     *
     * @return An array of events.
     */
    public EventType[] getEvents() {
        return events;
    }

    /**
     * Get the list of states the drone must be in in order to trigger the callback.
     *
     * @return A HashMap of states and their values. Can return null.
     */
    public HashMap<StateType, Object> getStates() {
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
    public boolean isForEvent(EventType event) {
        for (EventType e : events) {
            if (e == event) {
                return true;
            }
        }
        return false;
    }
}
