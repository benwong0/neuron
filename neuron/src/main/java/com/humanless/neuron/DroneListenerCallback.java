package com.humanless.neuron;

import java.util.List;

/**
 * Callback for DroneListener
 */
public interface DroneListenerCallback {
    /**
     * Runs callback.
     *
     * @param parameters A list of parameters containing results from the listener.
     *                   Parameters can be null.
     */
    void run(List<Object> parameters);
}
