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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.domain.SISPMSDevice;
import li.klass.fhem.service.ExecuteOnSuccess;
import li.klass.fhem.service.device.FS20Service;
import li.klass.fhem.service.device.SISPMSService;
import li.klass.fhem.service.room.RoomDeviceListListener;
import li.klass.fhem.service.room.RoomListService;

public class ToggleProvider extends AppWidgetProvider {
    
    public static final String ACTION_TOGGLE = "li.klass.fhem.appwidget.TOGGLE";
    public static final String INTENT_DEVICE_NAME = "deviceName";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            ToggleConfigurationActivity.updateWidget(appWidgetManager, context, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            ToggleConfigurationActivity.deleteWidget(context, appWidgetId);
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        
        if (intent.getAction().equals(ACTION_TOGGLE)) {
            final String deviceName = intent.getStringExtra(INTENT_DEVICE_NAME);

            final ExecuteOnSuccess executeOnSuccess = new ExecuteOnSuccess() {
                @Override
                public void onSuccess() {
                    String successString = context.getString(R.string.deviceSwitchSuccess);
                    successString = String.format(successString, deviceName);

                    Toast.makeText(context, successString, Toast.LENGTH_SHORT).show();
                }
            };
            
            RoomListService.INSTANCE.getAllRoomsDeviceList(context, false, new RoomDeviceListListener() {
                @Override
                public void onRoomListRefresh(RoomDeviceList roomDeviceList) {
                    Device device = roomDeviceList.getDeviceFor(deviceName);
                    if (device == null) return;

                    if (device instanceof FS20Device) {
                        FS20Service.INSTANCE.toggleState(null, (FS20Device) device, executeOnSuccess);
                    } else if (device instanceof SISPMSDevice) {
                        SISPMSService.INSTANCE.toggleState(null, (SISPMSDevice) device, executeOnSuccess);
                    } else {
                        throw new RuntimeException("unexpected device for toggling (" + device.getClass() + ")");
                    }
                }
            });
        }
    }

}
