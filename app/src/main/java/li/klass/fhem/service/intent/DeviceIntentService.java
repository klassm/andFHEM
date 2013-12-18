/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.service.intent;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.domain.GCMSendDevice;
import li.klass.fhem.domain.WOLDevice;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.fht.FHTMode;
import li.klass.fhem.domain.heating.ComfortTempDevice;
import li.klass.fhem.domain.heating.DesiredTempDevice;
import li.klass.fhem.domain.heating.EcoTempDevice;
import li.klass.fhem.domain.heating.HeatingDevice;
import li.klass.fhem.domain.heating.WindowOpenTempDevice;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.device.AtService;
import li.klass.fhem.service.device.DeviceService;
import li.klass.fhem.service.device.DimmableDeviceService;
import li.klass.fhem.service.device.FHTService;
import li.klass.fhem.service.device.GCMSendDeviceService;
import li.klass.fhem.service.device.GenericDeviceService;
import li.klass.fhem.service.device.HeatingService;
import li.klass.fhem.service.device.ToggleableService;
import li.klass.fhem.service.device.WOLService;
import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.GraphService;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.room.RoomListService;

import static li.klass.fhem.constants.Actions.DEVICE_DELETE;
import static li.klass.fhem.constants.Actions.DEVICE_DIM;
import static li.klass.fhem.constants.Actions.DEVICE_GRAPH;
import static li.klass.fhem.constants.Actions.DEVICE_MOVE_ROOM;
import static li.klass.fhem.constants.Actions.DEVICE_REFRESH_STATE;
import static li.klass.fhem.constants.Actions.DEVICE_REFRESH_VALUES;
import static li.klass.fhem.constants.Actions.DEVICE_RENAME;
import static li.klass.fhem.constants.Actions.DEVICE_RESET_WEEK_PROFILE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_ALIAS;
import static li.klass.fhem.constants.Actions.DEVICE_SET_COMFORT_TEMPERATURE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_DAY_TEMPERATURE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_DESIRED_TEMPERATURE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_ECO_TEMPERATURE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_MODE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_NIGHT_TEMPERATURE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_STATE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_SUB_STATE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_WEEK_PROFILE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_WINDOW_OPEN_TEMPERATURE;
import static li.klass.fhem.constants.Actions.DEVICE_TIMER_MODIFY;
import static li.klass.fhem.constants.Actions.DEVICE_TIMER_NEW;
import static li.klass.fhem.constants.Actions.DEVICE_TOGGLE_STATE;
import static li.klass.fhem.constants.Actions.DEVICE_WAKE;
import static li.klass.fhem.constants.Actions.DEVICE_WIDGET_TOGGLE;
import static li.klass.fhem.constants.Actions.GCM_ADD_SELF;
import static li.klass.fhem.constants.Actions.GCM_REMOVE_ID;
import static li.klass.fhem.constants.Actions.REDRAW_WIDGET;
import static li.klass.fhem.constants.Actions.RESEND_LAST_FAILED_COMMAND;
import static li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_ID;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_GRAPH_ENTRY_MAP;
import static li.klass.fhem.constants.BundleExtraKeys.STATE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.STATE_VALUE;
import static li.klass.fhem.service.intent.ConvenientIntentService.STATE.ERROR;
import static li.klass.fhem.service.intent.ConvenientIntentService.STATE.SUCCESS;

public class DeviceIntentService extends ConvenientIntentService {

    public DeviceIntentService() {
        super(DeviceIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {

        String deviceName = intent.getStringExtra(BundleExtraKeys.DEVICE_NAME);
        Device device = RoomListService.INSTANCE.getDeviceForName(deviceName, updatePeriod);
        Log.d(DeviceIntentService.class.getName(), intent.getAction());
        String action = intent.getAction();

        if (DEVICE_GRAPH.equals(action)) {
            return graphIntent(intent, device, resultReceiver);
        } else if (DEVICE_TOGGLE_STATE.equals(action)) {
            return toggleIntent(device);
        } else if (DEVICE_SET_STATE.equals(action)) {
            return setStateIntent(intent, device);
        } else if (DEVICE_DIM.equals(action)) {
            return dimIntent(intent, device);
        } else if (DEVICE_SET_DAY_TEMPERATURE.equals(action)) {
            double dayTemperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            FHTService.INSTANCE.setDayTemperature((FHTDevice) device, dayTemperature);
        } else if (DEVICE_SET_NIGHT_TEMPERATURE.equals(action)) {
            double nightTemperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            FHTService.INSTANCE.setNightTemperature((FHTDevice) device, nightTemperature);
        } else if (DEVICE_SET_MODE.equals(action)) {
            if (device instanceof FHTDevice) {
                FHTMode mode = (FHTMode) intent.getSerializableExtra(BundleExtraKeys.DEVICE_MODE);
                double desiredTemperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, FHTDevice.MINIMUM_TEMPERATURE);
                int holiday1 = intent.getIntExtra(BundleExtraKeys.DEVICE_HOLIDAY1, -1);
                int holiday2 = intent.getIntExtra(BundleExtraKeys.DEVICE_HOLIDAY2, -1);

                FHTService.INSTANCE.setMode((FHTDevice) device, mode, desiredTemperature, holiday1, holiday2);
            } else if (device instanceof HeatingDevice) {
                Enum mode = (Enum) intent.getSerializableExtra(BundleExtraKeys.DEVICE_MODE);
                HeatingDevice heatingDevice = (HeatingDevice) device;

                HeatingService.INSTANCE.setMode(heatingDevice, mode);
            }

        } else if (DEVICE_SET_WEEK_PROFILE.equals(action)) {
            if (!(device instanceof HeatingDevice)) return ERROR;
            HeatingService.INSTANCE.setWeekProfileFor((HeatingDevice) device);

        } else if (DEVICE_SET_WINDOW_OPEN_TEMPERATURE.equals(action)) {
            if (!(device instanceof WindowOpenTempDevice)) return SUCCESS;

            double temperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            HeatingService.INSTANCE.setWindowOpenTemp((WindowOpenTempDevice) device, temperature);

        } else if (DEVICE_SET_DESIRED_TEMPERATURE.equals(action)) {
            double temperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            if (device instanceof DesiredTempDevice) {
                HeatingService.INSTANCE.setDesiredTemperature((DesiredTempDevice) device, temperature);
            }

        } else if (DEVICE_SET_COMFORT_TEMPERATURE.equals(action)) {
            double temperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            if (device instanceof DesiredTempDevice) {
                HeatingService.INSTANCE.setComfortTemperature((ComfortTempDevice) device, temperature);
            }

        } else if (DEVICE_SET_ECO_TEMPERATURE.equals(action)) {
            double temperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            if (device instanceof DesiredTempDevice) {
                HeatingService.INSTANCE.setEcoTemperature((EcoTempDevice) device, temperature);
            }

        } else if (DEVICE_RESET_WEEK_PROFILE.equals(action)) {
            if (!(device instanceof HeatingDevice)) return ERROR;
            HeatingService.INSTANCE.resetWeekProfile((HeatingDevice) device);

        } else if (DEVICE_REFRESH_VALUES.equals(action)) {
            FHTService.INSTANCE.refreshValues((FHTDevice) device);

        } else if (DEVICE_RENAME.equals(action)) {
            String newName = intent.getStringExtra(BundleExtraKeys.DEVICE_NEW_NAME);
            DeviceService.INSTANCE.renameDevice(device, newName);

        } else if (DEVICE_DELETE.equals(action)) {
            DeviceService.INSTANCE.deleteDevice(device);

        } else if (DEVICE_MOVE_ROOM.equals(action)) {
            String newRoom = intent.getStringExtra(BundleExtraKeys.DEVICE_NEW_ROOM);
            DeviceService.INSTANCE.moveDevice(device, newRoom);

        } else if (DEVICE_SET_ALIAS.equals(action)) {
            String newAlias = intent.getStringExtra(BundleExtraKeys.DEVICE_NEW_ALIAS);
            DeviceService.INSTANCE.setAlias(device, newAlias);

        } else if (DEVICE_WAKE.equals(action)) {
            WOLService.INSTANCE.wake((WOLDevice) device);

        } else if (DEVICE_REFRESH_STATE.equals(action)) {
            WOLService.INSTANCE.requestRefreshState((WOLDevice) device);

        } else if (DEVICE_WIDGET_TOGGLE.equals(action)) {
            STATE result = toggleIntent(device);

            int widgetId = intent.getIntExtra(APP_WIDGET_ID, -1);
            Intent widgetUpdateIntent = new Intent(REDRAW_WIDGET);
            widgetUpdateIntent.putExtra(APP_WIDGET_ID, widgetId);
            startService(widgetUpdateIntent);

            return result;

        } else if (DEVICE_TIMER_MODIFY.equals(action)) {
            processTimerIntent(intent, true);

        } else if (DEVICE_TIMER_NEW.equals(action)) {
            processTimerIntent(intent, false);

        } else if (DEVICE_SET_SUB_STATE.equals(action)) {
            String name = intent.getStringExtra(STATE_NAME);
            String value = intent.getStringExtra(STATE_VALUE);

            GenericDeviceService.INSTANCE.setSubState(device, name, value);

        } else if (GCM_ADD_SELF.equals(action)) {
            GCMSendDeviceService.INSTANCE.addSelf((GCMSendDevice) device);

        } else if (GCM_REMOVE_ID.equals(action)) {
            String registrationId = intent.getStringExtra(BundleExtraKeys.GCM_REGISTRATION_ID);
            GCMSendDeviceService.INSTANCE.removeRegistrationId((GCMSendDevice) device, registrationId);

        } else if (RESEND_LAST_FAILED_COMMAND.equals(action)) {

            String lastFailedCommand = CommandExecutionService.INSTANCE.getLastFailedCommand();
            if ("xmllist".equalsIgnoreCase(lastFailedCommand)) {
                Intent updateIntent = new Intent(Actions.DO_UPDATE);
                updateIntent.putExtra(BundleExtraKeys.DO_REFRESH, true);
                sendBroadcast(updateIntent);
            } else {
                CommandExecutionService.INSTANCE.resendLastFailedCommand();
            }
        }

        return SUCCESS;
    }

    private STATE processTimerIntent(Intent intent, boolean isModify) {
        Bundle extras = intent.getExtras();

        assert extras != null;

        String targetDeviceName = extras.getString(BundleExtraKeys.TIMER_TARGET_DEVICE_NAME);
        String targetState = extras.getString(BundleExtraKeys.TIMER_TARGET_STATE);
        int hour = extras.getInt(BundleExtraKeys.TIMER_HOUR, 0);
        int minute = extras.getInt(BundleExtraKeys.TIMER_MINUTE, 0);
        int second = extras.getInt(BundleExtraKeys.TIMER_SECOND, 0);
        String repetition = extras.getString(BundleExtraKeys.TIMER_REPETITION);
        String type = extras.getString(BundleExtraKeys.TIMER_TYPE);
        String stateAppendix = extras.getString(BundleExtraKeys.TIMER_TARGET_STATE_APPENDIX);
        String timerName = extras.getString(BundleExtraKeys.TIMER_NAME);
        boolean isActive = extras.getBoolean(BundleExtraKeys.TIMER_IS_ACTIVE);

        if (isModify) {
            AtService.INSTANCE.modify(timerName, hour, minute, second, repetition, type, targetDeviceName, targetState, stateAppendix, isActive);
        } else {
            AtService.INSTANCE.createNew(timerName, hour, minute, second, repetition, type, targetDeviceName, targetState, stateAppendix, isActive);
        }

        return SUCCESS;
    }

    /**
     * Dim a device and notify the result receiver
     *
     * @param intent received intent
     * @param device device to dim
     * @return success?
     */
    private STATE dimIntent(Intent intent, Device device) {
        int dimProgress = intent.getIntExtra(BundleExtraKeys.DEVICE_DIM_PROGRESS, -1);
        if (device instanceof DimmableDevice) {
            DimmableDeviceService.INSTANCE.dim((DimmableDevice) device, dimProgress);
            return STATE.SUCCESS;
        }
        return STATE.ERROR;
    }

    /**
     * Set the state of a device and notify the result receiver
     *
     * @param intent received intent
     * @param device device to set the state on
     * @return success ?
     */
    private STATE setStateIntent(Intent intent, Device device) {
        String targetState = intent.getStringExtra(BundleExtraKeys.DEVICE_TARGET_STATE);
        GenericDeviceService.INSTANCE.setState(device, targetState);

        return STATE.SUCCESS;
    }

    /**
     * Toggle a device and notify the result receiver
     *
     * @param device device to toggle
     * @return success?
     */
    private STATE toggleIntent(Device device) {
        if (device instanceof ToggleableDevice && ((ToggleableDevice) device).supportsToggle()) {
            ToggleableService.INSTANCE.toggleState((ToggleableDevice) device);
            return SUCCESS;
        } else {
            return ERROR;
        }
    }

    /**
     * Find out graph data for a given device and notify the result receiver with the read graph data
     *
     * @param intent         received intent
     * @param device         device to read the graph data
     * @param resultReceiver receiver to notify on result
     * @return success?
     */
    private STATE graphIntent(Intent intent, Device device, ResultReceiver resultReceiver) {
        ArrayList<ChartSeriesDescription> seriesDescriptions = intent.getParcelableArrayListExtra(BundleExtraKeys.DEVICE_GRAPH_SERIES_DESCRIPTIONS);
        Calendar startDate = (Calendar) intent.getSerializableExtra(BundleExtraKeys.START_DATE);
        Calendar endDate = (Calendar) intent.getSerializableExtra(BundleExtraKeys.END_DATE);

        HashMap<ChartSeriesDescription, List<GraphEntry>> graphData = GraphService.INSTANCE.getGraphData(device, seriesDescriptions, startDate, endDate);
        sendSingleExtraResult(resultReceiver, ResultCodes.SUCCESS, DEVICE_GRAPH_ENTRY_MAP, graphData);
        return STATE.DONE;
    }
}
