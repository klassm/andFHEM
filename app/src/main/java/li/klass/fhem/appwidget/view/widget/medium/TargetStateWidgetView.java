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

package li.klass.fhem.appwidget.view.widget.medium;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.RemoteViews;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.WidgetConfigurationCreatedCallback;
import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.appwidget.view.widget.activity.TargetStateAdditionalInformationActivity;
import li.klass.fhem.appwidget.view.widget.base.AppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.util.ArrayUtil;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation.requiresAdditionalInformation;

public class TargetStateWidgetView extends AppWidgetView {
    @Override
    public int getWidgetName() {
        return R.string.widget_targetstate;
    }

    @Override
    protected int getContentView() {
        return R.layout.appwidget_targetstate;
    }

    @Override
    protected void fillWidgetView(Context context, RemoteViews view, Device<?> device, WidgetConfiguration widgetConfiguration) {
        String payload = widgetConfiguration.payload;
        String state = device.getEventMapStateFor(payload);

        view.setTextViewText(R.id.button, state);

        PendingIntent pendingIntent;
        if (requiresAdditionalInformation(state)) {
            Intent actionIntent = new Intent(context, TargetStateAdditionalInformationActivity.class);
            actionIntent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
            actionIntent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, payload);
            actionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            pendingIntent = PendingIntent.getActivity(context, widgetConfiguration.widgetId,
                    actionIntent, FLAG_UPDATE_CURRENT);
        } else {
            Intent actionIntent = new Intent(Actions.DEVICE_SET_STATE);
            actionIntent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
            actionIntent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, payload);

            pendingIntent = PendingIntent.getService(context, widgetConfiguration.widgetId, actionIntent,
                    FLAG_UPDATE_CURRENT);
        }

        view.setOnClickPendingIntent(R.id.button, pendingIntent);

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration);
    }

    @Override
    public void createWidgetConfiguration(final Context context, final WidgetType widgetType,
                                          final int appWidgetId, final Device device,
                                          final WidgetConfigurationCreatedCallback callback) {
        final String[] availableTargetStates = device.getAvailableTargetStates();
        String[] eventMapStates = device.getAvailableTargetStatesEventMapTexts();

        final AlertDialog.Builder contextMenu = new AlertDialog.Builder(context);
        contextMenu.setTitle(R.string.widget_targetstate);
        contextMenu.setItems(eventMapStates, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                dialogInterface.dismiss();

                String targetState = availableTargetStates[position];

                callback.widgetConfigurationCreated(new WidgetConfiguration(appWidgetId, device.getName(), widgetType,
                        targetState));
            }
        });
        contextMenu.show();
    }

    @Override
    public boolean supports(Device<?> device) {
        return !ArrayUtil.isEmpty(device.getAvailableTargetStates());
    }
}
