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
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.RemoteViews;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.view.widget.AppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DimmableDevice;

public class DimWidgetView extends AppWidgetView {


    @Override
    public int getWidgetName() {
        return R.string.widget_dim;
    }

    @Override
    protected int getContentView() {
        return R.layout.appwidget_dim;
    }

    @Override
    protected void fillWidgetView(final Context context, final RemoteViews view, final Device<?> device, final WidgetConfiguration widgetConfiguration) {
        final DimmableDevice dimmableDevice = (DimmableDevice) device;

        ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == ResultCodes.SUCCESS) {
                    Intent intent = new Intent(Actions.WIDGET_UPDATE);
                    intent.putExtra(BundleExtraKeys.APP_WIDGET_ID, widgetConfiguration.widgetId);
                    context.sendBroadcast(intent);
                }
            }
        };
        update(context, dimmableDevice, view, widgetConfiguration.widgetId, resultReceiver);

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration);
    }

    private void update(Context context, DimmableDevice device, RemoteViews view, int widgetId, ResultReceiver resultReceiver) {
        view.setTextViewText(R.id.state, device.getDimStateForPosition(device.getDimPosition()));

        Intent dimDownIntent = sendTargetDimState(device, "dimdown", resultReceiver);
        view.setOnClickPendingIntent(R.id.dimDown, PendingIntent.getService(context, (widgetId + "dimDown").hashCode(), dimDownIntent,
                PendingIntent.FLAG_UPDATE_CURRENT));

        Intent dimUpIntent = sendTargetDimState(device, "dimup", resultReceiver);
        view.setOnClickPendingIntent(R.id.dimUp, PendingIntent.getService(context, (widgetId + "dimUp").hashCode(), dimUpIntent,
                PendingIntent.FLAG_UPDATE_CURRENT));
    }

    @Override
    public boolean supports(Device<?> device) {
        return device instanceof DimmableDevice;
    }

    private Intent sendTargetDimState(DimmableDevice device, String targetState, ResultReceiver resultReceiver) {

        Intent intent = new Intent(Actions.DEVICE_SET_STATE);
        intent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, targetState);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, resultReceiver);

        return intent;
    }
}
