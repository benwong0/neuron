package com.humanless.neuron.dji;

import com.humanless.neuron.DroneState;

/**
 * Drone state for DJI drones.
 */
public class DjiDroneState extends DroneState {
    @Override
    public boolean isInStates(int[] states) {
        return false;
    }
}
