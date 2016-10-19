package com.humanless.neuron;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DroneDispatcher test.
 */
@RunWith(MockitoJUnitRunner.class)
public class DroneDispatcherTest {
    @Mock
    private DroneListener<String, String> listener;

    @Test
    public void dispatch() {
        DroneDispatcher<String, String> dispatcher = new DroneDispatcher<String, String>() {
        };

        HashMap<String, Object> states = new HashMap<>();
        states.put("state1", "1");
        String event = "event";
        String[] events = new String[]{event};

        when(listener.getCallback()).thenReturn(new Runnable() {
            @Override
            public void run() {
            }
        });
        when(listener.getStates()).thenReturn(states);
        when(listener.getEvents()).thenReturn(events);

        dispatcher.addListener(listener);

        dispatcher.getDroneStateManager().setState("state1", "2");
        when(listener.isForEvent(event)).thenReturn(true);
        dispatcher.dispatch(event);
        verify(listener, times(0)).getCallback();

        dispatcher.getDroneStateManager().setState("state1", "1");
        when(listener.isForEvent(event)).thenReturn(false);
        dispatcher.dispatch(event);
        verify(listener, times(0)).getCallback();

        dispatcher.getDroneStateManager().setState("state1", "2");
        when(listener.isForEvent(event)).thenReturn(false);
        dispatcher.dispatch(event);
        verify(listener, times(0)).getCallback();

        dispatcher.getDroneStateManager().setState("state1", "1");
        when(listener.isForEvent(event)).thenReturn(true);
        dispatcher.dispatch(event);
        verify(listener, times(1)).getCallback();

        // Without listener
        reset(listener);

        when(listener.getCallback()).thenReturn(new Runnable() {
            @Override
            public void run() {
            }
        });
        when(listener.getStates()).thenReturn(states);
        when(listener.getEvents()).thenReturn(events);

        dispatcher.removeListener(listener);
        dispatcher.getDroneStateManager().setState("state1", "1");
        when(listener.isForEvent(event)).thenReturn(true);
        dispatcher.dispatch(event);
        verify(listener, times(0)).getCallback();
    }
}
