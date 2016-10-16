package com.humanless.neuron;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
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
    private DroneListener droneListener;

    @Mock
    private DroneState droneState;

    @Test
    public void dispatch() {
        DroneDispatcher dispatcher = new DroneDispatcher();
        int event = 0;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

            }
        };

        dispatcher.setDroneState(droneState);
        dispatcher.addListener(droneListener);

        when(droneListener.getCallback()).thenReturn(runnable);

        when(droneListener.isForEvent(event)).thenReturn(true);
        when(droneState.isInStates(any(int[].class))).thenReturn(true);
        dispatcher.dispatch(event);
        verify(droneListener, times(1)).getCallback();
        reset(droneListener);

        when(droneListener.isForEvent(event)).thenReturn(false);
        when(droneState.isInStates(any(int[].class))).thenReturn(false);
        dispatcher.dispatch(event);
        verify(droneListener, times(0)).getCallback();

        when(droneListener.isForEvent(event)).thenReturn(true);
        when(droneState.isInStates(any(int[].class))).thenReturn(false);
        dispatcher.dispatch(event);
        verify(droneListener, times(0)).getCallback();

        when(droneListener.isForEvent(event)).thenReturn(false);
        when(droneState.isInStates(any(int[].class))).thenReturn(true);
        dispatcher.dispatch(event);
        verify(droneListener, times(0)).getCallback();
    }
}
