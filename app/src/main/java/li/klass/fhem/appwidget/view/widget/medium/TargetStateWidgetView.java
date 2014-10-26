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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import org.apache.commons.lang3.StringUtils;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesDialogUtil;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.WidgetConfigurationCreatedCallback;
import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.appwidget.view.widget.activity.TargetStateAdditionalInformationActivity;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.service.intent.DeviceIntentService;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation.requiresAdditionalInformation;

public class TargetStateWidgetView extends DeviceAppWidgetView {
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
        String payload = widgetConfiguration.payload.get(1);
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
            actionIntent.setClass(context, DeviceIntentService.class);
            actionIntent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
            actionIntent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, payload);

            pendingIntent = PendingIntent.getService(context, widgetConfiguration.widgetId, actionIntent,
                    FLAG_UPDATE_CURRENT);
        }

        view.setOnClickPendingIntent(R.id.button, pendingIntent);

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration);
    }


    @Override
    protected void createDeviceWidgetConfiguration(Context context, final WidgetType widgetType,
                                                   final int appWidgetId, Device device,
                                                   final WidgetConfigurationCreatedCallback callback) {
        AvailableTargetStatesDialogUtil.showSwitchOptionsMenu(context, device,
                new AvailableTargetStatesDialogUtil.TargetStateSelectedCallback() {
                    @Override
                    public <D extends Device<D>> void onTargetStateSelected(String state, String subState,
                                                                            D device, Context context) {
                        if (state.equals("state")) {
                            state = subState;
                            subState = null;
                        }

                        String toSet = state;
                        if (!StringUtils.isBlank(subState)) {
                            toSet += " " + subState;
                        }

                        callback.widgetConfigurationCreated(new WidgetConfiguration(appWidgetId,
                                widgetType, device.getName(), toSet));
                    }
                }
        );
    }

    @Override
    public boolean supports(Device<?> device) {
        return !device.getSetList().getEntries().isEmpty();
    }
}
