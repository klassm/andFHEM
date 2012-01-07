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

package li.klass.fhem.appwidget.toggle;

import android.app.ListActivity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.DeviceType;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.service.room.RoomDeviceListListener;
import li.klass.fhem.service.room.RoomListService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.appwidget.AppWidgetManager.*;

public class ToggleConfigurationActivity extends ListActivity {
    private int widgetId;
    private static final String preferenceName = ToggleConfigurationActivity.class.getName();

    private List<DeviceType> switchableDeviceTypes = Arrays.asList(DeviceType.FS20, DeviceType.SIS_PMS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        widgetId = intent.getExtras().getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);


        if (! intent.getAction().equals(ACTION_APPWIDGET_CONFIGURE) || widgetId == INVALID_APPWIDGET_ID) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        RoomListService.INSTANCE.getAllRoomsDeviceList(this, false, new RoomDeviceListListener() {
            @Override
            public void onRoomListRefresh(RoomDeviceList roomDeviceList) {
                List<Device> switchableDevices = new ArrayList<Device>();
                for (DeviceType switchableDeviceType : switchableDeviceTypes) {
                    switchableDevices.addAll(roomDeviceList.getDevicesOfType(switchableDeviceType));
                }

                List<String> deviceNames = new ArrayList<String>();
                for (Device switchableDevice : switchableDevices) {
                    deviceNames.add(switchableDevice.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ToggleConfigurationActivity.this, android.R.layout.simple_list_item_1, deviceNames);
                setListAdapter(adapter);
            }
        });
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        TextView content = (TextView) view;
        String deviceName = (String) content.getText();

        saveWidgetIdToPreferences(deviceName);

        updateWidget(AppWidgetManager.getInstance(this), this, widgetId);

        Intent toggleIntent = new Intent();
        toggleIntent.putExtra(EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, toggleIntent);
        finish();
    }

    public static void updateWidget(AppWidgetManager appWidgetManager, Context context, int appWidgetId) {
        String deviceName = getWidgetDeviceName(context, appWidgetId);
        if (deviceName == null) return;

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_switch);

        views.setTextViewText(R.id.switchButton, deviceName);

        Intent intent = new Intent(context, ToggleProvider.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(ToggleProvider.ACTION_TOGGLE);

        intent.putExtras(new Bundle());
        intent.putExtra(ToggleProvider.INTENT_DEVICE_NAME, deviceName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.switchButton, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    public static void deleteWidget(Context context, int appWidgetId) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        editor.remove(String.valueOf(appWidgetId));
    }

    private void saveWidgetIdToPreferences(String deviceName) {
        SharedPreferences.Editor edit = getSharedPreferencesEditor(this);
        edit.putString(String.valueOf(widgetId), deviceName);
        edit.commit();
    }

    private static String getWidgetDeviceName(Context context, int widgetId) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getString(String.valueOf(widgetId), null);
    }

    private static SharedPreferences.Editor getSharedPreferencesEditor(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.edit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }
}
