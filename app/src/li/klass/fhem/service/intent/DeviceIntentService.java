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
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.domain.WOLDevice;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.fht.FHTMode;
import li.klass.fhem.domain.floorplan.Coordinate;
import li.klass.fhem.service.device.*;
import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.GraphService;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.room.RoomListService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static li.klass.fhem.constants.Actions.*;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_GRAPH_ENTRY_MAP;
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
        if (action.equals(DEVICE_GRAPH)) {
            return graphIntent(intent, device, resultReceiver);
        } else if (action.equals(DEVICE_TOGGLE_STATE)) {
            return toggleIntent(device);
        } else if (action.equals(DEVICE_SET_STATE)) {
            return setStateIntent(intent, device);
        } else if (action.equals(DEVICE_DIM)) {
            return dimIntent(intent, device);
        } else if (action.equals(DEVICE_SET_DAY_TEMPERATURE)) {
            double dayTemperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            FHTService.INSTANCE.setDayTemperature((FHTDevice) device, dayTemperature);
        } else if (action.equals(DEVICE_SET_NIGHT_TEMPERATURE)) {
            double nightTemperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            FHTService.INSTANCE.setNightTemperature((FHTDevice) device, nightTemperature);
        } else if (action.equals(DEVICE_SET_MODE)) {
            FHTMode mode = (FHTMode) intent.getSerializableExtra(BundleExtraKeys.DEVICE_MODE);
            FHTService.INSTANCE.setMode((FHTDevice) device, mode);
        } else if (action.equals(DEVICE_SET_TIMETABLE)) {
            FHTService.INSTANCE.setTimetableFor((FHTDevice) device);
        } else if (action.equals(DEVICE_SET_WINDOW_OPEN_TEMPERATURE)) {
            double temperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            FHTService.INSTANCE.setWindowOpenTemp((FHTDevice) device, temperature);
        } else if (action.equals(DEVICE_SET_DESIRED_TEMPERATURE)) {
            double temperature = intent.getDoubleExtra(BundleExtraKeys.DEVICE_TEMPERATURE, -1);
            FHTService.INSTANCE.setDesiredTemperature((FHTDevice) device, temperature);
        } else if (action.equals(DEVICE_RESET_TIMETABLE)) {
            FHTService.INSTANCE.resetTimetable((FHTDevice) device);
        } else if (action.equals(DEVICE_REFRESH_VALUES)) {
            FHTService.INSTANCE.refreshValues((FHTDevice) device);
        } else if (action.equals(DEVICE_RENAME)) {
            String newName = intent.getStringExtra(BundleExtraKeys.DEVICE_NEW_NAME);
            DeviceService.INSTANCE.renameDevice(device, newName);
        } else if (action.equals(DEVICE_DELETE)) {
            DeviceService.INSTANCE.deleteDevice(device);
        } else if (action.equals(DEVICE_MOVE_ROOM)) {
            String newRoom = intent.getStringExtra(BundleExtraKeys.DEVICE_NEW_ROOM);
            DeviceService.INSTANCE.moveDevice(device, newRoom);
        } else if (action.equals(DEVICE_SET_ALIAS)) {
            String newAlias = intent.getStringExtra(BundleExtraKeys.DEVICE_NEW_ALIAS);
            DeviceService.INSTANCE.setAlias(device, newAlias);
        } else if (action.equals(DEVICE_WAKE)) {
            WOLService.INSTANCE.wake((WOLDevice) device);
        } else if (action.equals(DEVICE_REFRESH_STATE)) {
            WOLService.INSTANCE.requestRefreshState((WOLDevice) device);
        } else if (action.equals(DEVICE_FLOORPLAN_MOVE)) {
            moveFloorplanDevice(intent, device);
        } else if (action.equals(DEVICE_WIDGET_TOGGLE)) {
            STATE result = toggleIntent(device);

            int widgetId = intent.getIntExtra(BundleExtraKeys.APP_WIDGET_ID, -1);
            Intent widgetUpdateIntent = new Intent(Actions.WIDGET_UPDATE);
            widgetUpdateIntent.putExtra(BundleExtraKeys.APP_WIDGET_ID, widgetId);
            sendBroadcast(widgetUpdateIntent);

            return result;
        } else if (action.equals(Actions.DEVICE_TIMER_MODIFY)) {
           processTimerIntent(intent, true);
        } else if (action.equals(Actions.DEVICE_TIMER_NEW)) {
            processTimerIntent(intent, false);
        }
        return SUCCESS;
    }

    private STATE processTimerIntent(Intent intent, boolean isModify) {
        Bundle extras = intent.getExtras();
        String targetDeviceName = extras.getString(BundleExtraKeys.TIMER_TARGET_DEVICE_NAME);
        String targetState = extras.getString(BundleExtraKeys.TIMER_TARGET_STATE);
        int hour = extras.getInt(BundleExtraKeys.TIMER_HOUR, 0);
        int minute = extras.getInt(BundleExtraKeys.TIMER_MINUTE, 0);
        int second = extras.getInt(BundleExtraKeys.TIMER_SECOND, 0);
        String repetition = extras.getString(BundleExtraKeys.TIMER_REPETITION);
        String type = extras.getString(BundleExtraKeys.TIMER_TYPE);
        String stateAppendix = extras.getString(BundleExtraKeys.TIMER_TARGET_STATE_APPENDIX, null);
        String timerName = extras.getString(BundleExtraKeys.TIMER_NAME);

        if (isModify) {
            AtService.INSTANCE.modify(timerName, hour, minute, second, repetition, type, targetDeviceName, targetState, stateAppendix);
        } else {
            AtService.INSTANCE.createNew(timerName, hour, minute, second, repetition, type, targetDeviceName, targetState, stateAppendix);
        }

        return SUCCESS;
    }

    private STATE moveFloorplanDevice(Intent intent, Device device) {
        String floorplanName = intent.getStringExtra(BundleExtraKeys.FLOORPLAN_NAME);
        Coordinate coordinate = (Coordinate) intent.getSerializableExtra(BundleExtraKeys.COORDINATE);

        FloorplanService.INSTANCE.setDeviceLocation(floorplanName, device, coordinate);
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
        if (device instanceof FS20Device) {
            FS20Service.INSTANCE.dim((FS20Device) device, dimProgress);
            return STATE.SUCCESS;
        } else if (device instanceof CULHMDevice && ((CULHMDevice) device).getSubType() == CULHMDevice.SubType.DIMMER) {
            CULHMService.INSTANCE.dim((CULHMDevice) device, dimProgress);
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
     * @param intent received intent
     * @param device device to read the graph data
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
