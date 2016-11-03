package com.humanless.neuron.dji;

import android.content.Context;

import com.humanless.neuron.DroneDispatcher;
import com.humanless.neuron.DroneStateManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dji.common.airlink.DJISignalInformation;
import dji.common.battery.DJIBatteryState;
import dji.common.camera.CameraSystemState;
import dji.common.camera.DJICameraExposureParameters;
import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.DJIFlightControllerCurrentState;
import dji.common.flightcontroller.DJILocationCoordinate2D;
import dji.common.flightcontroller.DJILocationCoordinate3D;
import dji.common.gimbal.DJIGimbalState;
import dji.common.util.DJICommonCallbacks;
import dji.sdk.airlink.DJIAirLink;
import dji.sdk.airlink.DJILBAirLink;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.base.DJIDiagnostics;
import dji.sdk.battery.DJIBattery;
import dji.sdk.camera.DJICamera;
import dji.sdk.camera.DJIMedia;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.flightcontroller.DJIFlightControllerDelegate;
import dji.sdk.gimbal.DJIGimbal;
import dji.sdk.products.DJIAircraft;
import dji.sdk.sdkmanager.DJISDKManager;

import static android.R.id.list;

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
                stateManager.setState(DjiDroneState.PRODUCT_REGISTRATION, error == DJISDKError.REGISTRATION_SUCCESS);
                dispatch(DjiDroneEvent.PRODUCT_REGISTRATION);
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

                    setupProductListener(product);
                    setupAircraftStateListener(product);
                    setupBatteryListener(product);
                    setupCameraStateListener(product);
                    setupCameraVideoDataListener(product);
                    setupCameraExposureListener(product);
                    setupCameraMediaListener(product);
                    setupDiagnosticListener(product);
                    setupControllerSignalListener(product);

                    dispatch(DjiDroneEvent.PRODUCT_CHANGE);
                }
            }
        };

        DJISDKManager.getInstance().initSDKManager(context, djisdkManagerCallback);
    }

    // region Basic product listener

    private void setupProductListener(DJIBaseProduct djiBaseProduct) {
        final DroneStateManager<DjiDroneState> stateManager = getDroneStateManager();
        djiBaseProduct.setDJIBaseProductListener(new DJIBaseProduct.DJIBaseProductListener() {
            @Override
            public void onComponentChange(DJIBaseProduct.DJIComponentKey djiComponentKey, DJIBaseComponent djiBaseComponent, DJIBaseComponent djiBaseComponent1) {
                dispatch(DjiDroneEvent.PRODUCT_COMPONENT_CHANGE);
            }

            @Override
            public void onProductConnectivityChanged(boolean b) {
                stateManager.setState(DjiDroneState.PRODUCTION_CONNECTION, b);
                dispatch(DjiDroneEvent.PRODUCT_CONNECTION_CHANGE);
            }
        });
    }

    // endregion

    // region Remote controller listener

    private void setupControllerSignalListener(DJIBaseProduct djiBaseProduct) {
        DJIAirLink airLink = djiBaseProduct.getAirLink();
        if (airLink != null && airLink.isLBAirLinkSupported()) {
            DJILBAirLink lbAirLink = airLink.getLBAirLink();
            if (lbAirLink != null) {
                lbAirLink.setLBAirLinkUpdatedRemoteControllerSignalInformationCallback(new DJILBAirLink.DJILBAirLinkUpdatedRemoteControllerSignalInformationCallback() {
                    @Override
                    public void onResult(ArrayList<DJISignalInformation> arrayList) {
                        DroneStateManager<DjiDroneState> stateManager = getDroneStateManager();
                        boolean stateChanged = false;

                        List exist = (List) stateManager.getState(DjiDroneState.CONTROLLER_SIGNAL);
                        int existCnt = exist == null ? 0 : exist.size();
                        int listCnt = arrayList == null ? 0 : arrayList.size();

                        if (existCnt != listCnt) {
                            stateChanged = true;
                        } else {
                            for (int i = 0; i < existCnt; i++) {
                                DJISignalInformation oldSignal = (DJISignalInformation) exist.get(i);
                                DJISignalInformation newSignal = arrayList.get(i);
                                if (oldSignal.getPercent() != newSignal.getPercent() || oldSignal.getPower() != newSignal.getPower()) {
                                    stateChanged = true;
                                    break;
                                }
                            }
                        }

                        if (stateChanged) {
                            stateManager.setState(DjiDroneState.CONTROLLER_SIGNAL, list);
                            dispatch(DjiDroneEvent.CONTROLLER_SIGNAL_CHANGE);
                        }
                    }
                });
            }
        }
    }

    // endregion

    // region Diagnostic related listeners

    private void setupDiagnosticListener(DJIBaseProduct djiBaseProduct) {
        djiBaseProduct.setUpdateDiagnosticsListCallback(new DJIDiagnostics.UpdateDiagnosticsListCallback() {
            @Override
            public void onDiagnosticsListUpdate(List<DJIDiagnostics> list) {
                DroneStateManager<DjiDroneState> stateManager = getDroneStateManager();
                boolean stateChanged = false;

                List exist = (List) stateManager.getState(DjiDroneState.AIRCRAFT_DIAGNOSTICS);

                int existCnt = exist == null ? 0 : exist.size();
                int listCnt = list == null ? 0 : list.size();

                if (existCnt != listCnt) {
                    stateChanged = true;
                } else {
                    for (int i = 0; i < existCnt; i++) {
                        DJIDiagnostics oldMsg = (DJIDiagnostics) exist.get(i);
                        DJIDiagnostics newMsg = list.get(i);
                        if (oldMsg.getCode() != newMsg.getCode()) {
                            stateChanged = true;
                            break;
                        }
                    }
                }

                if (stateChanged) {
                    stateManager.setState(DjiDroneState.AIRCRAFT_DIAGNOSTICS, list);
                    dispatch(DjiDroneEvent.AIRCRAFT_DIAGNOSTIC);
                }
            }
        });
    }

    // endregion

    // region Gimbal listeners

    private void setupGimbalStateListener(DJIBaseProduct djiBaseProduct) {
        djiBaseProduct.getGimbal().setGimbalStateUpdateCallback(new DJIGimbal.GimbalStateUpdateCallback() {
            @Override
            public void onGimbalStateUpdate(DJIGimbal djiGimbal, DJIGimbalState djiGimbalState) {
                DroneStateManager<DjiDroneState> stateManager = getDroneStateManager();
                boolean stateChanged = false;

                if (stateManager.setState(DjiDroneState.GIMBAL_CALIBRATING, djiGimbalState.isCalibrating())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.GIMBAL_CALIBRATIION_SUCCESS, djiGimbalState.isCalibrationSuccess())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.GIMBAL_ATTITUDE_RESET, djiGimbalState.isAttitudeReset())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.GIMBAL_MOBILE_DEVICE_MOUNTED, djiGimbalState.isMobileDeviceMounted())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.GIMBAL_MOTOR_OVERLOADED, djiGimbalState.isMotorOverloaded())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.GIMBAL_PITCH_AT_STOP, djiGimbalState.isPitchAtStop())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.GIMBAL_ROLL_AT_STOP, djiGimbalState.isRollAtStop())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.GIMBAL_YAW_AT_STOP, djiGimbalState.isYawAtStop())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.GIMBAL_TESTING_BALANCE, djiGimbalState.isTestingBalance())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.GIMBAL_ATTITUDE_IN_DEGREES, djiGimbalState.getAttitudeInDegrees())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.GIMBAL_BALANCE_STATE, djiGimbalState.getBalanceState())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.GIMBAL_PITCH_TEST_RESULT, djiGimbalState.getPitchTestResult())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.GIMBAL_ROLL_FINE_TUNE_IN_DEGREES, djiGimbalState.getRollFineTuneInDegrees())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.GIMBAL_ROLL_TEST_RESULT, djiGimbalState.getRollTestResult())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.GIMBAL_WORK_MODE, djiGimbalState.getWorkMode())) {
                    stateChanged = true;
                }

                if (stateChanged) {
                    dispatch(DjiDroneEvent.AIRCRAFT_DIAGNOSTIC);
                }
            }
        });
    }

    // endregion

    // region Camera related listeners

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

    private void setupCameraMediaListener(DJIBaseProduct djiBaseProduct) {
        DJICamera camera = djiBaseProduct.getCamera();
        if (camera == null) {
            return;
        }

        camera.setDJICameraGeneratedNewMediaFileCallback(new DJICamera.CameraGeneratedNewMediaFileCallback() {
            @Override
            public void onResult(DJIMedia djiMedia) {
                DroneStateManager<DjiDroneState> stateManager = getDroneStateManager();

                DJIMedia currentMedia = (DJIMedia) stateManager.getState(DjiDroneState.CAMERA_MEDIA);
                if (currentMedia != null && djiMedia.getFileName().equals(currentMedia.getFileName())) {
                    stateManager.setState(DjiDroneState.CAMERA_MEDIA, djiMedia);
                    dispatch(DjiDroneEvent.CAMERA_MEDIA);
                }
            }
        });
    }

    private void setupCameraExposureListener(DJIBaseProduct djiBaseProduct) {
        DJICamera camera = djiBaseProduct.getCamera();
        if (camera == null) {
            return;
        }

        camera.setCameraUpdatedCurrentExposureValuesCallback(new DJICamera.CameraUpdatedCurrentExposureValuesCallback() {
            @Override
            public void onResult(DJICameraExposureParameters djiCameraExposureParameters) {
                DroneStateManager<DjiDroneState> stateManager = getDroneStateManager();
                boolean stateChanged = false;

                if (stateManager.setState(DjiDroneState.CAMERA_APERTURE, djiCameraExposureParameters.getAperture())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.CAMERA_EXPOSURE_COMPENSATION, djiCameraExposureParameters.getExposureCompensation())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.CAMERA_ISO, djiCameraExposureParameters.getISO())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.CAMERA_SHUTTER_SPEED, djiCameraExposureParameters.getShutterSpeed())) {
                    stateChanged = true;
                }

                if (stateChanged) {
                    dispatch(DjiDroneEvent.CAMERA_EXPOSURE_CHANGE);
                }
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

    // endregion

    // region Battery related listeners

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

    // endregion

    // region Aircraft related listeners

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
                if (stateManager.setState(DjiDroneState.AIRCRAFT_HOME_LOCATION_LATITUDE, homeLocation.getLatitude())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.AIRCRAFT_HOME_LOCATION_LONGITUDE, homeLocation.getLongitude())) {
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
                if (stateManager.isInState(DjiDroneState.AIRCRAFT_FLYING, false, 2) && isFlying) {
                    // Took off
                    stateManager.setState(DjiDroneState.AIRCRAFT_FLYING, true);
                    dispatch(DjiDroneEvent.AIRCRAFT_TAKE_OFF);
                    stateChanged = true;
                } else if (stateManager.isInState(DjiDroneState.AIRCRAFT_FLYING, true, 2) && !isFlying) {
                    // Landed
                    stateManager.setState(DjiDroneState.AIRCRAFT_FLYING, false);
                    dispatch(DjiDroneEvent.AIRCRAFT_LAND);
                    stateChanged = true;
                }

                // Velocities
                if (stateManager.setState(DjiDroneState.AIRCRAFT_VELOCITY_X, djiFlightControllerCurrentState.getVelocityX())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.AIRCRAFT_VELOCITY_Y, djiFlightControllerCurrentState.getVelocityY())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.AIRCRAFT_VELOCITY_Z, djiFlightControllerCurrentState.getVelocityZ())) {
                    stateChanged = true;
                }

                // Other status
                if (stateManager.setState(DjiDroneState.AIRCRAFT_FAIL_SAFE, djiFlightControllerCurrentState.isFailsafe())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.AIRCRAFT_RETURNING_HOME, djiFlightControllerCurrentState.isGoingHome())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.AIRCRAFT_HOME_LOCATION, djiFlightControllerCurrentState.isHomePointSet())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.AIRCRAFT_IMU_PREHEATING, djiFlightControllerCurrentState.isIMUPreheating())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.AIRCRAFT_MULTI_MODE_OPEN, djiFlightControllerCurrentState.isMultipModeOpen())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.AIRCRAFT_LIMITED_HEIGHT_REACHED, djiFlightControllerCurrentState.isReachLimitedHeight())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.AIRCRAFT_LIMITED_RADIUS_REACHED, djiFlightControllerCurrentState.isReachLimitedRadius())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.AIRCRAFT_ULTRASONIC, djiFlightControllerCurrentState.isUltrasonicBeingUsed())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.AIRCRAFT_ULTRASONIC_ERROR, djiFlightControllerCurrentState.isUltrasonicError())) {
                    stateChanged = true;
                }
                if (stateManager.setState(DjiDroneState.AIRCRAFT_VISION_SENSOR, djiFlightControllerCurrentState.isVisionSensorBeingUsed())) {
                    stateChanged = true;
                }

                // Only dispatch if state has been changed
                if (stateChanged) {
                    dispatch(DjiDroneEvent.AIRCRAFT_STATE_CHANGE);
                }
            }
        });
    }

    // endregion
}
