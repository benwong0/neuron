package com.humanless.neuron.dji;

import android.content.Context;

import com.humanless.neuron.DroneDispatcher;
import com.humanless.neuron.DroneStateManager;

import java.util.ArrayList;
import java.util.Arrays;

import dji.common.battery.DJIBatteryState;
import dji.common.camera.CameraSystemState;
import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.DJIFlightControllerCurrentState;
import dji.common.flightcontroller.DJILocationCoordinate2D;
import dji.common.flightcontroller.DJILocationCoordinate3D;
import dji.common.util.DJICommonCallbacks;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.battery.DJIBattery;
import dji.sdk.camera.DJICamera;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.flightcontroller.DJIFlightControllerDelegate;
import dji.sdk.products.DJIAircraft;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * Drone dispatcher for DJI SDK.
 */
public class DjiDroneDispatcher extends DroneDispatcher<DjiDroneEvent, DjiDroneState> {
    public DjiDroneDispatcher(Context context) {
        setupConnectionListener(context);
    }

    private void setupConnectionListener(Context context) {
        final DroneStateManager<DjiDroneState> stateManager = getDroneStateManager();
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
                        camera.getSerialNumber(new DJICommonCallbacks.DJICompletionCallbackWith<String>() {
                            @Override
                            public void onSuccess(String s) {
                                stateManager.setState(DjiDroneState.CAMERA_SERIAL, s);
                            }

                            @Override
                            public void onFailure(DJIError djiError) {
                            }
                        });
                    }
                    stateManager.setState(DjiDroneState.PRODUCTION_CONNECTION, product.isConnected());

                    dispatch(DjiDroneEvent.PRODUCT_CHANGE);
                    setupProductListener(product);
                    setupAircraftStateListener(product);
                    setupBatteryListener(product);
                    setupCameraStateListener(product);
                    setupCameraVideoDataListener(product);
                }
            }
        };

        DJISDKManager.getInstance().initSDKManager(context, djisdkManagerCallback);
    }

    private void setupProductListener(DJIBaseProduct djiBaseProduct) {
        final DroneStateManager<DjiDroneState> stateManager = getDroneStateManager();
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

    private void setupCameraVideoDataListener(DJIBaseProduct djiBaseProduct) {
        DJICamera camera = djiBaseProduct.getCamera();
        if (camera == null) {
            return;
        }

        camera.setDJICameraReceivedVideoDataCallback(new DJICamera.CameraReceivedVideoDataCallback() {
            @Override
            public void onResult(byte[] bytes, int i) {
                dispatch(DjiDroneEvent.CAMERA_VIDEO_FEED, Arrays.asList((Object) bytes, i));
            }
        });
    }

    private void setupCameraStateListener(DJIBaseProduct djiBaseProduct) {
        DJICamera camera = djiBaseProduct.getCamera();
        if (camera == null) {
            return;
        }

        camera.setDJICameraUpdatedSystemStateCallback(new DJICamera.CameraUpdatedSystemStateCallback() {
            @Override
            public void onResult(CameraSystemState cameraSystemState) {
                DroneStateManager<DjiDroneState> stateManager = getDroneStateManager();
                boolean stateChanged = false;

                if (stateManager.setState(DjiDroneState.CAMERA_MODE, cameraSystemState.getCameraMode())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.CAMERA_RECORDING_TIME, cameraSystemState.getCurrentVideoRecordingTimeInSeconds())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.CAMERA_ERROR, cameraSystemState.isCameraError())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.CAMERA_OVER_HEAT, cameraSystemState.isCameraOverHeated())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.CAMERA_RECORDING, cameraSystemState.isRecording())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.CAMERA_SHOOTING_BURST_PHOTO, cameraSystemState.isShootingBurstPhoto())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.CAMERA_SHOOTING_INTERVAL_PHOTO, cameraSystemState.isShootingIntervalPhoto())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.CAMERA_SHOOTING_SINGLE_PHOTO, cameraSystemState.isShootingSinglePhoto())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.CAMERA_SHOOTING_SINGLE_PHOTO_RAW, cameraSystemState.isShootingSinglePhotoInRAWFormat())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.CAMERA_STORING_PHOTO, cameraSystemState.isStoringPhoto())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.CAMERA_USB_MODE, cameraSystemState.isUSBMode())) {
                    stateChanged = true;
                }

                if (stateChanged) {
                    dispatch(DjiDroneEvent.CAMERA_STATE_CHANGE);
                }
            }
        });
    }

    private void setupBatteryListener(DJIBaseProduct djiBaseProduct) {
        ArrayList<DJIBattery> batteries = djiBaseProduct.getBatteries();

        if (batteries.size() <= 0) {
            return;
        }

        for (DJIBattery battery : batteries) {
            battery.setBatteryStateUpdateCallback(new DJIBattery.DJIBatteryStateUpdateCallback() {
                @Override
                public void onResult(DJIBatteryState djiBatteryState) {
                    DroneStateManager<DjiDroneState> stateManager = getDroneStateManager();
                    boolean stateChanged = false;

                    if (stateManager.setState(DjiDroneState.BATTERY_ENERGY_REMAINING_PERCENT, djiBatteryState.getBatteryEnergyRemainingPercent())) {
                        stateChanged = true;
                    }
                    if (stateManager.setState(DjiDroneState.BATTERY_TEMPERATURE, djiBatteryState.getBatteryTemperature())) {
                        stateChanged = true;
                    }
                    if (stateManager.setState(DjiDroneState.BATTERY_CURRENT, djiBatteryState.getCurrentCurrent())) {
                        stateChanged = true;
                    }
                    if (stateManager.setState(DjiDroneState.BATTERY_ENERGY, djiBatteryState.getCurrentEnergy())) {
                        stateChanged = true;
                    }
                    if (stateManager.setState(DjiDroneState.BATTERY_VOLTAGE, djiBatteryState.getCurrentVoltage())) {
                        stateChanged = true;
                    }
                    if (stateManager.setState(DjiDroneState.BATTERY_TOTAL_ENERGY, djiBatteryState.getFullChargeEnergy())) {
                        stateChanged = true;
                    }
                    if (stateManager.setState(DjiDroneState.BATTERY_LIFETIME_REMAINING_PERCENT, djiBatteryState.getLifetimeRemainingPercent())) {
                        stateChanged = true;
                    }
                    if (stateManager.setState(DjiDroneState.BATTERY_DISCHARGE_COUNT, djiBatteryState.getNumberOfDischarge())) {
                        stateChanged = true;
                    }
                    if (stateManager.setState(DjiDroneState.BATTERY_CHARGING, djiBatteryState.isBeingCharged())) {
                        stateChanged = true;
                    }

                    if (stateChanged) {
                        dispatch(DjiDroneEvent.BATTERY_STATE_CHANGE);
                    }
                }
            });
        }
    }

    private void setupAircraftStateListener(DJIBaseProduct djiBaseProduct) {
        if (!(djiBaseProduct instanceof DJIAircraft)) {
            return;
        }

        DJIFlightController flightController = ((DJIAircraft) djiBaseProduct).getFlightController();
        flightController.setUpdateSystemStateCallback(new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {
            @Override
            public void onResult(DJIFlightControllerCurrentState djiFlightControllerCurrentState) {
                DroneStateManager<DjiDroneState> stateManager = getDroneStateManager();
                boolean stateChanged = false;

                // Home location
                DJILocationCoordinate2D homeLocation = djiFlightControllerCurrentState.getHomeLocation();
                if (stateManager.setState(DjiDroneState.HOME_LOCATION_LATITUDE, homeLocation.getLatitude())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.HOME_LOCATION_LONGITUDE, homeLocation.getLongitude())) {
                    stateChanged = true;
                }

                // Aircraft location
                DJILocationCoordinate3D aircraftLocation = djiFlightControllerCurrentState.getAircraftLocation();
                if (stateManager.setState(DjiDroneState.AIRCRAFT_LOCATION_LATITUDE, aircraftLocation.getLatitude())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.AIRCRAFT_LOCATION_LONGITUDE, aircraftLocation.getLongitude())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.AIRCRAFT_LOCATION_ALTITUDE, aircraftLocation.getAltitude())) {
                    stateChanged = true;
                }

                // Take off/landing
                boolean isFlying = djiFlightControllerCurrentState.isFlying();
                if (stateManager.isInState(DjiDroneState.FLYING, false, 2) && isFlying) {
                    // Took off
                    stateManager.setState(DjiDroneState.FLYING, true);
                    dispatch(DjiDroneEvent.TAKE_OFF);
                    stateChanged = true;
                } else if (stateManager.isInState(DjiDroneState.FLYING, true, 2) && !isFlying) {
                    // Landed
                    stateManager.setState(DjiDroneState.FLYING, false);
                    dispatch(DjiDroneEvent.LAND);
                    stateChanged = true;
                }

                // Velocities
                if (stateManager.setState(DjiDroneState.VELOCITY_X, djiFlightControllerCurrentState.getVelocityX())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.VELOCITY_Y, djiFlightControllerCurrentState.getVelocityY())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.VELOCITY_Z, djiFlightControllerCurrentState.getVelocityZ())) {
                    stateChanged = true;
                }

                // Other status
                if (stateManager.setState(DjiDroneState.FAIL_SAFE, djiFlightControllerCurrentState.isFailsafe())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.RETURNING_HOME, djiFlightControllerCurrentState.isGoingHome())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.HOME_LOCATION, djiFlightControllerCurrentState.isHomePointSet())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.IMU_PREHEATING, djiFlightControllerCurrentState.isIMUPreheating())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.MULTI_MODE_OPEN, djiFlightControllerCurrentState.isMultipModeOpen())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.LIMITED_HEIGHT_REACHED, djiFlightControllerCurrentState.isReachLimitedHeight())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.LIMITED_RADIUS_REACHED, djiFlightControllerCurrentState.isReachLimitedRadius())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.ULTRASONIC, djiFlightControllerCurrentState.isUltrasonicBeingUsed())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.ULTRASONIC_ERROR, djiFlightControllerCurrentState.isUltrasonicError())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.VISION_SENSOR, djiFlightControllerCurrentState.isVisionSensorBeingUsed())) {
                    stateChanged = true;
                }

                // Only dispatch if state has been changed
                if (stateChanged) {
                    dispatch(DjiDroneEvent.FLIGHT_STATE_CHANGE);
                }
            }
        });
    }
}
