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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.service.intent.NotificationIntentService;
import li.klass.fhem.util.FhemResultReceiver;

public class NotificationSettingView {

    private final Context context;
    private final String deviceName;
    private View view;

    public NotificationSettingView(Context context, String deviceName) {
        this.context = context;
        this.deviceName = deviceName;
    }

    @SuppressLint("InflateParams")
    private View createView(int value) {
        view = LayoutInflater.from(context).inflate(R.layout.notification_device_settings, null);
        RadioButton allUpdates = (RadioButton) view.findViewById(R.id.allUpdates);
        RadioButton stateUpdates = (RadioButton) view.findViewById(R.id.stateUpdates);
        RadioButton noUpdates = (RadioButton) view.findViewById(R.id.noUpdates);

        switch (value) {
            case NotificationIntentService.ALL_UPDATES:
                allUpdates.setChecked(true);
                break;
            case NotificationIntentService.STATE_UPDATES:
                stateUpdates.setChecked(true);
                break;
            case NotificationIntentService.NO_UPDATES:
                noUpdates.setChecked(true);
                break;
        }

        return view;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setTitle(deviceName)
                .setView(createView(value))
                .setPositiveButton(R.string.okButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                int updateType = getUpdateType();

                                context.startService(new Intent(Actions.NOTIFICATION_SET_FOR_DEVICE)
                                        .setClass(context, NotificationIntentService.class)
                                        .putExtra(BundleExtraKeys.DEVICE_NAME, deviceName)
                                        .putExtra(BundleExtraKeys.NOTIFICATION_UPDATES, updateType));
                            }
                        }
                )
                .setNegativeButton(R.string.cancelButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();
        builder.show();
    }

    private int getUpdateType() {
        RadioButton allUpdates = (RadioButton) view.findViewById(R.id.allUpdates);
        RadioButton stateUpdates = (RadioButton) view.findViewById(R.id.stateUpdates);
        RadioButton noUpdates = (RadioButton) view.findViewById(R.id.noUpdates);

        if (allUpdates.isChecked()) return NotificationIntentService.ALL_UPDATES;
        if (stateUpdates.isChecked()) return NotificationIntentService.STATE_UPDATES;
        if (noUpdates.isChecked()) return NotificationIntentService.NO_UPDATES;
        return 0;
    }
}
