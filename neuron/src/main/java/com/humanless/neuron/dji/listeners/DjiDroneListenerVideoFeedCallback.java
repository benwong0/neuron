package com.humanless.neuron.dji.listeners;

import com.humanless.neuron.DroneListenerCallback;

import java.util.List;

/**
 * DJI video feed listener callback. This callback translates the returned parameters into separate
 * variables.
 */
public abstract class DjiDroneListenerVideoFeedCallback implements DroneListenerCallback {
    public abstract void run(byte[] bytes, int i);

    @Override
    public void run(List<Object> parameters) {
        if (parameters.size() == 2) {
            if (parameters.get(0) instanceof byte[] && Integer.class.isInstance(parameters.get(1))) {
                run((byte[]) parameters.get(0), (int) parameters.get(1));
            }
        }
    }
}
