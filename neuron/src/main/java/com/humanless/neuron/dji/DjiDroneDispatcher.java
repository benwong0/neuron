package com.humanless.neuron.dji;

import android.content.Context;

import com.humanless.neuron.DroneDispatcher;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.DJIFlightControllerCurrentState;
import dji.common.flightcontroller.DJILocationCoordinate2D;
import dji.common.flightcontroller.DJILocationCoordinate3D;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.camera.DJICamera;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.flightcontroller.DJIFlightControllerDelegate;
import dji.sdk.products.DJIAircraft;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * Drone dispatcher for DJI SDK.
 */
public class DjiDroneDispatcher extends DroneDispatcher {
    public DjiDroneDispatcher(Context context) {
        setupConnectionListener(context);

        setDroneStateManager(new DjiDroneStateManager());
    }

    private void setupConnectionListener(Context context) {
        final DjiDroneStateManager stateManager = (DjiDroneStateManager) getDroneStateManager();
        DJISDKManager.DJISDKManagerCallback djisdkManagerCallback = new DJISDKManager.DJISDKManagerCallback() {
            @Override
            public void onGetRegisteredResult(DJIError error) {
                stateManager.setState(DjiDroneState.REGISTRATION, error == DJISDKError.REGISTRATION_SUCCESS);
                dispatch(DjiDroneEvent.REGISTRATION);
            }

            @Override
            public void onProductChanged(DJIBaseProduct oldProduct, DJIBaseProduct newProduct) {
                DJIBaseProduct product = DJISDKManager.getInstance().getDJIProduct();

                if (product != null) {
                    stateManager.setState(DjiDroneState.PRODUCT_NAME, product.getModel().name());

                    DJICamera camera = product.getCamera();
                    if (camera != null) {
                        stateManager.setState(DjiDroneState.CAMERA_NAME, camera.getDisplayName());
                    }
                    stateManager.setState(DjiDroneState.PRODUCTION_CONNECTION, product.isConnected());

                    dispatch(DjiDroneEvent.PRODUCT_CHANGE);
                    setupProductListener(product);
                    setupAircraftStateListener(product);

                }
            }
        };

        DJISDKManager.getInstance().initSDKManager(context, djisdkManagerCallback);
    }

    private void setupProductListener(DJIBaseProduct djiBaseProduct) {
        final DjiDroneStateManager stateManager = (DjiDroneStateManager) getDroneStateManager();
        djiBaseProduct.setDJIBaseProductListener(new DJIBaseProduct.DJIBaseProductListener() {
            @Override
            public void onComponentChange(DJIBaseProduct.DJIComponentKey djiComponentKey, DJIBaseComponent djiBaseComponent, DJIBaseComponent djiBaseComponent1) {
                dispatch(DjiDroneEvent.COMPONENT_CHANGE);
            }

            @Override
            public void onProductConnectivityChanged(boolean b) {
                stateManager.setState(DjiDroneState.PRODUCTION_CONNECTION, b);
                dispatch(DjiDroneEvent.PRODUCT_CONNECTION_CHANGE);
            }
        });
    }

    private void setupAircraftStateListener(DJIBaseProduct djiBaseProduct) {
        if (!(djiBaseProduct instanceof DJIAircraft)) {
            return;
        }

        final DJIFlightController flightController = ((DJIAircraft) djiBaseProduct).getFlightController();
        flightController.setUpdateSystemStateCallback(new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {
            @Override
            public void onResult(DJIFlightControllerCurrentState djiFlightControllerCurrentState) {
                DjiDroneStateManager stateManager = (DjiDroneStateManager) getDroneStateManager();
                boolean stateChanged = false;

                // Home location
                DJILocationCoordinate2D homeLocation = djiFlightControllerCurrentState.getHomeLocation();
                if (!stateManager.isInState(DjiDroneState.HOME_LOCATION_LATITUDE, homeLocation.getLatitude())) {
                    stateManager.setState(DjiDroneState.HOME_LOCATION_LATITUDE, homeLocation.getLatitude());
                    stateChanged = true;
                }
                if (!stateManager.isInState(DjiDroneState.HOME_LOCATION_LONGITUDE, homeLocation.getLongitude())) {
                    stateManager.setState(DjiDroneState.HOME_LOCATION_LONGITUDE, homeLocation.getLongitude());
                    stateChanged = true;
                }

                // Aircraft location
                DJILocationCoordinate3D aircraftLocation = djiFlightControllerCurrentState.getAircraftLocation();
                if (!stateManager.isInState(DjiDroneState.AIRCRAFT_LOCATION_LATITUDE, aircraftLocation.getLatitude())) {
                    stateManager.setState(DjiDroneState.AIRCRAFT_LOCATION_LATITUDE, aircraftLocation.getLatitude());
                    stateChanged = true;
                }
                if (!stateManager.isInState(DjiDroneState.AIRCRAFT_LOCATION_LONGITUDE, aircraftLocation.getLongitude())) {
                    stateManager.setState(DjiDroneState.AIRCRAFT_LOCATION_LONGITUDE, aircraftLocation.getLongitude());
                    stateChanged = true;
                }
                if (!stateManager.isInState(DjiDroneState.AIRCRAFT_LOCATION_ALTITUDE, aircraftLocation.getAltitude())) {
                    stateManager.setState(DjiDroneState.AIRCRAFT_LOCATION_ALTITUDE, aircraftLocation.getAltitude());
                    stateChanged = true;
                }

                // Take off/landing
                boolean isFlying = djiFlightControllerCurrentState.isFlying();
                if (!stateManager.isInState(DjiDroneState.FLYING, isFlying)) {
                    if (isFlying) {
                        // Took off
                        dispatch(DjiDroneEvent.TAKE_OFF);
                    } else {
                        // Landed
                        dispatch(DjiDroneEvent.LAND);
                    }
                    stateManager.setState(DjiDroneState.FLYING, isFlying);
                    stateChanged = true;
                }

                // Only dispatch if state has been changed
                if (stateChanged) {
                    dispatch(DjiDroneEvent.FLIGHT_STATE_CHANGE);
                }
            }
        });
    }

    private void dispatch(DjiDroneEvent event) {
        dispatch(event.ordinal());
    }
}
