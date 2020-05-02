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

package li.klass.fhem.widget.notification;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.service.NotificationService;
import li.klass.fhem.service.intent.NotificationIntentService;
import li.klass.fhem.util.FhemResultReceiver;

public class NotificationSettingView {
    private static final int[] VALUES = new int[] {
            NotificationService.ALL_UPDATES, NotificationService.STATE_UPDATES,
            NotificationService.NO_UPDATES
    };
    private static final int[] DESCRIPTION_IDS = new int[] {
        R.string.notificationAllUpdates, R.string.notificationStateUpdates,
        R.string.notificationNoUpdates
    };

    private final Context context;
    private final String deviceName;

    public NotificationSettingView(Context context, String deviceName) {
        this.context = context;
        this.deviceName = deviceName;
    }

    public void show(Context context) {
        context.startService(new Intent(Actions.NOTIFICATION_GET_FOR_DEVICE)
                .setClass(context, NotificationIntentService.class)
                .putExtra(BundleExtraKeys.DEVICE_NAME, deviceName)
                .putExtra(BundleExtraKeys.RESULT_RECEIVER, new FhemResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == ResultCodes.SUCCESS && resultData != null &&
                                resultData.containsKey(BundleExtraKeys.NOTIFICATION_UPDATES)) {
                            int value = resultData.getInt(BundleExtraKeys.NOTIFICATION_UPDATES);
                            showWith(value);
                        }
                    }
                }));
    }

    private void showWith(int value) {
        int selected = -1;
        CharSequence[] descriptions = new CharSequence[DESCRIPTION_IDS.length];
        for (int i = 0; i < DESCRIPTION_IDS.length; i++) {
            descriptions[i] = context.getString(DESCRIPTION_IDS[i]);
            if (value == VALUES[i]) {
                selected = i;
            }
        }

        new AlertDialog.Builder(context)
                .setTitle(deviceName)
                .setSingleChoiceItems(descriptions, selected, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.startService(new Intent(Actions.NOTIFICATION_SET_FOR_DEVICE)
                                .setClass(context, NotificationIntentService.class)
                                .putExtra(BundleExtraKeys.DEVICE_NAME, deviceName)
                                .putExtra(BundleExtraKeys.NOTIFICATION_UPDATES, VALUES[which]));
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
