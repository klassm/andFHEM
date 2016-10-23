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
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.RemoteViews;

import com.google.common.base.Optional;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.service.AppWidgetUpdateService;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.intent.DeviceIntentService;

public class DimWidgetView extends DeviceAppWidgetView {


    @Override
    public int getWidgetName() {
        return R.string.widget_dim;
    }

    @Override
    protected int getContentView() {
        return R.layout.appwidget_dim;
    }

    @Override
    protected void fillWidgetView(final Context context, final RemoteViews view, final FhemDevice<?> device, final WidgetConfiguration widgetConfiguration) {
        final DimmableDevice dimmableDevice = (DimmableDevice) device;

        ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == ResultCodes.SUCCESS) {
                    Intent intent = new Intent(Actions.REDRAW_WIDGET);
                    intent.setClass(context, AppWidgetUpdateService.class);
                    intent.putExtra(BundleExtraKeys.APP_WIDGET_ID, widgetConfiguration.widgetId);
                    context.startService(intent);
                }
            }
        };
        update(context, dimmableDevice, view, widgetConfiguration.widgetId, widgetConfiguration.connectionId, resultReceiver);

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration, context);
    }

    private void update(Context context, DimmableDevice device, RemoteViews view, int widgetId, Optional<String> connectionId, ResultReceiver resultReceiver) {
        view.setTextViewText(R.id.state, device.getDimStateNameForDimStateValue(device.getDimPosition()));

        Intent dimDownIntent = sendTargetDimState(context, device, "dimdown", connectionId, resultReceiver);
        view.setOnClickPendingIntent(R.id.dimDown, PendingIntent.getService(context, (widgetId + "dimDown").hashCode(), dimDownIntent,
                PendingIntent.FLAG_UPDATE_CURRENT));

        Intent dimUpIntent = sendTargetDimState(context, device, "dimup", connectionId, resultReceiver);
        view.setOnClickPendingIntent(R.id.dimUp, PendingIntent.getService(context, (widgetId + "dimUp").hashCode(), dimUpIntent,
                PendingIntent.FLAG_UPDATE_CURRENT));
    }

    @Override
    public boolean supports(FhemDevice<?> device) {
        return (device instanceof DimmableDevice) && ((DimmableDevice) device).supportsDim();
    }

    private Intent sendTargetDimState(Context context, DimmableDevice device, String targetState, Optional<String> connectionId, ResultReceiver resultReceiver) {

        Intent intent = new Intent(Actions.DEVICE_SET_STATE)
                .setClass(context, DeviceIntentService.class)
                .putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, targetState)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                .putExtra(BundleExtraKeys.RESULT_RECEIVER, resultReceiver)
                .putExtra(BundleExtraKeys.CONNECTION_ID, connectionId.orNull());

        return intent;
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }
}
