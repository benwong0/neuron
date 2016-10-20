package com.humanless.neuron;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * DroneStateManager test.
 */

@RunWith(MockitoJUnitRunner.class)
public class DroneStateManagerTest {
    @Test
    public void stateGetterSetter() {
        DroneStateManager<String> stateManager = new DroneStateManager<>();
        stateManager.setState("1", 1);

        assertThat((Integer) stateManager.getState("1"), equalTo(1));
        assertThat(stateManager.getState("2"), nullValue());
    }

    @Test
    public void isInState() {
        DroneStateManager<String> stateManager = new DroneStateManager<>();
        stateManager.setState("state1", 1);

        String state = "state1";
        HashMap<String, Object> states = new HashMap<>();
        states.put(state, 1);

        assertThat(stateManager.isInStates(states), equalTo(true));
        assertThat(stateManager.isInState(state, states.get(state)), equalTo(true));

        states.put(state, 2);
        assertThat(stateManager.isInStates(states), equalTo(false));
        assertThat(stateManager.isInState(state, states.get(state)), equalTo(false));
    }
}
