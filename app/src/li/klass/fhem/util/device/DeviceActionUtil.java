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

package li.klass.fhem.util.device;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.EditText;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;

public class DeviceActionUtil {
    private static ResultReceiver updateReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            AndFHEMApplication.getContext().sendBroadcast(new Intent(Actions.DO_UPDATE));
        }
    };

    public static void renameDevice(final Context context, final Device device) {
        final EditText input = new EditText(context);
        input.setText(device.getName());
        new AlertDialog.Builder(context)
                .setTitle(R.string.context_rename)
                .setView(input)
                .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newName = input.getText().toString();

                        Intent intent = new Intent(Actions.DEVICE_RENAME);
                        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                        intent.putExtra(BundleExtraKeys.DEVICE_NEW_NAME, newName);
                        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, updateReceiver);
                        context.startService(intent);
                    }
                }).setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    public static void deleteDevice(final Context context, final Device device) {
        showConfirmation(context, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Intent intent = new Intent(Actions.DEVICE_DELETE);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, updateReceiver);
                context.startService(intent);
            }
        }, context.getString(R.string.deleteConfirmation));
    }

    public static void moveDevice(final  Context context,final Device device) {
        final EditText input = new EditText(context);
        input.setText(device.getRoomConcatenated());
        new AlertDialog.Builder(context)
                .setTitle(R.string.context_move)
                .setView(input)
                .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newRoom = input.getText().toString();

                        Intent intent = new Intent(Actions.DEVICE_MOVE_ROOM);
                        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                        intent.putExtra(BundleExtraKeys.DEVICE_NEW_ROOM, newRoom);
                        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, updateReceiver);
                        context.startService(intent);
                    }
                }).setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    public static void setAlias(final Context context, final Device device) {
        final EditText input = new EditText(context);
        input.setText(device.getAlias());
        new AlertDialog.Builder(context)
                .setTitle(R.string.context_alias)
                .setView(input)
                .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newAlias = input.getText().toString();

                        Intent intent = new Intent(Actions.DEVICE_SET_ALIAS);
                        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                        intent.putExtra(BundleExtraKeys.DEVICE_NEW_ALIAS, newAlias);
                        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, updateReceiver);
                        context.startService(intent);
                    }
                }).setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    public static void showConfirmation(final Context context, DialogInterface.OnClickListener positiveOnClickListener) {
        showConfirmation(context, positiveOnClickListener, null);
    }

    public static void showConfirmation(final Context context, DialogInterface.OnClickListener positiveOnClickListener, String text) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.areYouSure)
                .setMessage(text)
                .setPositiveButton(R.string.okButton, positiveOnClickListener)
                .setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        context.sendBroadcast(new Intent(Actions.DO_UPDATE));
                    }
                }).show();
    }
}
