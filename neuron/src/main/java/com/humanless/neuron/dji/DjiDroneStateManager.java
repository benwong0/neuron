package com.humanless.neuron.dji;

import com.humanless.neuron.DroneStateManager;

/**
 * Drone state manager for DJI drones.
 */
public class DjiDroneStateManager extends DroneStateManager {
    @Override
    public boolean isInStates(int[] states) {
        return false;
    }
}
