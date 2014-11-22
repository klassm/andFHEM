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

import com.google.common.base.Optional;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.appwidget.service.AppWidgetUpdateService;
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
import static li.klass.fhem.constants.Actions.DEVICE_WIDGET_TOGGLE;
import static li.klass.fhem.constants.Actions.GCM_ADD_SELF;
import static li.klass.fhem.constants.Actions.GCM_REMOVE_ID;
import static li.klass.fhem.constants.Actions.RESEND_LAST_FAILED_COMMAND;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_GRAPH_ENTRY_MAP;
import static li.klass.fhem.constants.BundleExtraKeys.STATE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.STATE_VALUE;
import static li.klass.fhem.service.intent.ConvenientIntentService.STATE.DONE;
import static li.klass.fhem.service.intent.ConvenientIntentService.STATE.ERROR;
import static li.klass.fhem.service.intent.ConvenientIntentService.STATE.SUCCESS;
import static li.klass.fhem.service.room.RoomListService.RemoteUpdateRequired.REQUIRED;

public class DeviceIntentService extends ConvenientIntentService {

    @Inject
    RoomListService roomListService;
    @Inject
    FHTService fhtService;
    @Inject
    HeatingService heatingService;
    @Inject
    DeviceService deviceService;
    @Inject
    GenericDeviceService genericDeviceService;
    @Inject
    GCMSendDeviceService gcmSendDeviceService;
    @Inject
    CommandExecutionService commandExecutionService;
    @Inject
    AtService atService;
    @Inject
    WOLService wolService;
    @Inject
    DimmableDeviceService dimmableDeviceService;
    @Inject
    ToggleableService toggleableService;
    @Inject
    GraphService graphService;
    @Inject
    NotificationIntentService notificationIntentService;

    private static final Logger LOG = LoggerFactory.getLogger(DeviceIntentService.class);

    public DeviceIntentService() {
        super(DeviceIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {

        if (roomListService.updateRoomDeviceListIfRequired(intent, updatePeriod) == REQUIRED) {
            return DONE;
        }

        String deviceName = intent.getStringExtra(BundleExtraKeys.DEVICE_NAME);
        Optional<Device> deviceOptional = roomListService.getDeviceForName(deviceName);
        if (!deviceOptional.isPresent()) {
            LOG.info("handleIntent() - cannot find device for {}", deviceName);
        }
        Device device = deviceOptional.orNull();

        Log.d(DeviceIntentService.class.getName(), intent.getAction());
        String action = intent.getAction();

        STATE result = STATE.SUCCESS;
        if (DEVICE_GRAPH.equals(action)) {
            result = graphIntent(intent, device, resultReceiver);
        } else if (DEVICE_TOGGLE_STATE.equals(action)) {
            result = toggleIntent(device);
        } else if (DEVICE_SET_STATE.equals(action)) {
            result = setStateIntent(intent, device);
        } else if (DEVICE_DIM.equals(action)) {
            result = dimIntent(intent, device);
        } else if (DEVICE_SET_DAY_TEMPERATURE.equals(action)) {
            double dayTemperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            fhtService.setDayTemperature((FHTDevice) device, dayTemperature);
        } else if (DEVICE_SET_NIGHT_TEMPERATURE.equals(action)) {
            double nightTemperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            fhtService.setNightTemperature((FHTDevice) device, nightTemperature);
        } else if (DEVICE_SET_MODE.equals(action)) {
            if (device instanceof FHTDevice) {
                FHTMode mode = (FHTMode) intent.getSerializableExtra(BundleExtraKeys.DEVICE_MODE);
                double desiredTemperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, FHTDevice.MINIMUM_TEMPERATURE);
                int holiday1 = intent.getIntExtra(BundleExtraKeys.DEVICE_HOLIDAY1, -1);
                int holiday2 = intent.getIntExtra(BundleExtraKeys.DEVICE_HOLIDAY2, -1);

                fhtService.setMode((FHTDevice) device, mode, desiredTemperature, holiday1, holiday2);
            } else if (device instanceof HeatingDevice) {
                Enum mode = (Enum) intent.getSerializableExtra(BundleExtraKeys.DEVICE_MODE);
                HeatingDevice heatingDevice = (HeatingDevice) device;

                heatingService.setMode(heatingDevice, mode);
            }

        } else if (DEVICE_SET_WEEK_PROFILE.equals(action)) {
            if (!(device instanceof HeatingDevice)) return ERROR;
            heatingService.setWeekProfileFor((HeatingDevice) device);

        } else if (DEVICE_SET_WINDOW_OPEN_TEMPERATURE.equals(action)) {
            if (!(device instanceof WindowOpenTempDevice)) return SUCCESS;

            double temperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            heatingService.setWindowOpenTemp((WindowOpenTempDevice) device, temperature);

        } else if (DEVICE_SET_DESIRED_TEMPERATURE.equals(action)) {
            double temperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            if (device instanceof DesiredTempDevice) {
                heatingService.setDesiredTemperature((DesiredTempDevice) device, temperature);
            }

        } else if (DEVICE_SET_COMFORT_TEMPERATURE.equals(action)) {
            double temperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            if (device instanceof DesiredTempDevice) {
                heatingService.setComfortTemperature((ComfortTempDevice) device, temperature);
            }

        } else if (DEVICE_SET_ECO_TEMPERATURE.equals(action)) {
            double temperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            if (device instanceof DesiredTempDevice) {
                heatingService.setEcoTemperature((EcoTempDevice) device, temperature);
            }

        } else if (DEVICE_RESET_WEEK_PROFILE.equals(action)) {
            if (!(device instanceof HeatingDevice)) return ERROR;
            heatingService.resetWeekProfile((HeatingDevice) device);

        } else if (DEVICE_REFRESH_VALUES.equals(action)) {
            fhtService.refreshValues((FHTDevice) device);

        } else if (DEVICE_RENAME.equals(action)) {
            String newName = intent.getStringExtra(BundleExtraKeys.DEVICE_NEW_NAME);
            deviceService.renameDevice(device, newName);
            notificationIntentService.rename(device.getName(), newName);

        } else if (DEVICE_DELETE.equals(action)) {
            deviceService.deleteDevice(device);

        } else if (DEVICE_MOVE_ROOM.equals(action)) {
            String newRoom = intent.getStringExtra(BundleExtraKeys.DEVICE_NEW_ROOM);
            deviceService.moveDevice(device, newRoom);

        } else if (DEVICE_SET_ALIAS.equals(action)) {
            String newAlias = intent.getStringExtra(BundleExtraKeys.DEVICE_NEW_ALIAS);
            deviceService.setAlias(device, newAlias);

        } else if (DEVICE_REFRESH_STATE.equals(action)) {
            wolService.requestRefreshState((WOLDevice) device);
        } else if (DEVICE_WIDGET_TOGGLE.equals(action)) {
            result = toggleIntent(device);

        } else if (DEVICE_TIMER_MODIFY.equals(action)) {
            processTimerIntent(intent, true);

        } else if (DEVICE_TIMER_NEW.equals(action)) {
            processTimerIntent(intent, false);

        } else if (DEVICE_SET_SUB_STATE.equals(action)) {
            String name = intent.getStringExtra(STATE_NAME);
            String value = intent.getStringExtra(STATE_VALUE);

            genericDeviceService.setSubState(device, name, value);

        } else if (GCM_ADD_SELF.equals(action)) {
            gcmSendDeviceService.addSelf((GCMSendDevice) device);

        } else if (GCM_REMOVE_ID.equals(action)) {
            String registrationId = intent.getStringExtra(BundleExtraKeys.GCM_REGISTRATION_ID);
            gcmSendDeviceService.removeRegistrationId((GCMSendDevice) device, registrationId);

        } else if (RESEND_LAST_FAILED_COMMAND.equals(action)) {

            String lastFailedCommand = commandExecutionService.getLastFailedCommand();
            if ("xmllist".equalsIgnoreCase(lastFailedCommand)) {
                Intent updateIntent = new Intent(Actions.DO_UPDATE);
                updateIntent.putExtra(BundleExtraKeys.DO_REFRESH, true);
                sendBroadcast(updateIntent);
            } else {
                commandExecutionService.resendLastFailedCommand();
            }
        }

        Intent redrawWidgetsIntent = new Intent(Actions.REDRAW_ALL_WIDGETS);
        redrawWidgetsIntent.setClass(this, AppWidgetUpdateService.class);
        startService(redrawWidgetsIntent);

        return result;
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
        DateTime startDate = (DateTime) intent.getSerializableExtra(BundleExtraKeys.START_DATE);
        DateTime endDate = (DateTime) intent.getSerializableExtra(BundleExtraKeys.END_DATE);

        HashMap<ChartSeriesDescription, List<GraphEntry>> graphData = graphService.getGraphData(device, seriesDescriptions, startDate, endDate);
        sendSingleExtraResult(resultReceiver, ResultCodes.SUCCESS, DEVICE_GRAPH_ENTRY_MAP, graphData);
        return STATE.DONE;
    }

    /**
     * Toggle a device and notify the result receiver
     *
     * @param device device to toggle
     * @return success?
     */
    private STATE toggleIntent(Device device) {
        if (device instanceof ToggleableDevice && ((ToggleableDevice) device).supportsToggle()) {
            toggleableService.toggleState((ToggleableDevice) device);
            return SUCCESS;
        } else {
            return ERROR;
        }
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
        genericDeviceService.setState(device, targetState);

        return STATE.SUCCESS;
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
            dimmableDeviceService.dim((DimmableDevice) device, dimProgress);
            return STATE.SUCCESS;
        }
        return STATE.ERROR;
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
        String timerName = extras.getString(BundleExtraKeys.DEVICE_NAME);
        boolean isActive = extras.getBoolean(BundleExtraKeys.TIMER_IS_ACTIVE);

        if (isModify) {
            atService.modify(timerName, hour, minute, second, repetition, type, targetDeviceName, targetState, stateAppendix, isActive);
        } else {
            atService.createNew(timerName, hour, minute, second, repetition, type, targetDeviceName, targetState, stateAppendix, isActive);
        }

        return SUCCESS;
    }
}
