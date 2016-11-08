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
import android.widget.EditText;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.intent.DeviceIntentService;

public class DeviceActionUtil {

    public static void renameDevice(final Context context, final FhemDevice device) {
        final EditText input = new EditText(context);
        input.setText(device.getName());
        new AlertDialog.Builder(context)
                .setTitle(R.string.context_rename)
                .setView(input)
                .setPositiveButton(R.string.okButton, (dialog, whichButton) -> {
                    String newName = input.getText().toString();

                    context.startService(new Intent(Actions.DEVICE_RENAME)
                            .setClass(context, DeviceIntentService.class)
                            .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                            .putExtra(BundleExtraKeys.DEVICE_NEW_NAME, newName)
                            .putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context)));
                }).setNegativeButton(R.string.cancelButton, (dialog, whichButton) -> {
        }).show();
    }

    public static void deleteDevice(final Context context, final FhemDevice device) {
        final String deviceName = device.getName();
        showConfirmation(context, (dialog, whichButton) -> context.startService(new Intent(Actions.DEVICE_DELETE)
                .setClass(context, DeviceIntentService.class)
                .putExtra(BundleExtraKeys.DEVICE_NAME, deviceName)
                .putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context))), context.getString(R.string.deleteConfirmation));
    }

    public static void showConfirmation(final Context context, DialogInterface.OnClickListener positiveOnClickListener, String text) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.areYouSure)
                .setMessage(text)
                .setPositiveButton(R.string.okButton, positiveOnClickListener)
                .setNegativeButton(R.string.cancelButton, (dialog, whichButton) -> context.sendBroadcast(new Intent(Actions.DO_UPDATE))).show();
    }

    public static void moveDevice(final Context context, final FhemDevice device) {
        final EditText input = new EditText(context);
        input.setText(device.getRoomConcatenated());
        new AlertDialog.Builder(context)
                .setTitle(R.string.context_move)
                .setView(input)
                .setPositiveButton(R.string.okButton, (dialog, whichButton) -> {
                    String newRoom = input.getText().toString();

                    context.startService(new Intent(Actions.DEVICE_MOVE_ROOM)
                            .setClass(context, DeviceIntentService.class)
                            .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                            .putExtra(BundleExtraKeys.DEVICE_NEW_ROOM, newRoom)
                            .putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context)));
                }).setNegativeButton(R.string.cancelButton, (dialog, whichButton) -> {
        }).show();
    }

    public static void setAlias(final Context context, final FhemDevice device) {
        final EditText input = new EditText(context);
        input.setText(device.getAlias());
        new AlertDialog.Builder(context)
                .setTitle(R.string.context_alias)
                .setView(input)
                .setPositiveButton(R.string.okButton, (dialog, whichButton) -> {
                    String newAlias = input.getText().toString();

                    context.startService(new Intent(Actions.DEVICE_SET_ALIAS)
                            .setClass(context, DeviceIntentService.class)
                            .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                            .putExtra(BundleExtraKeys.DEVICE_NEW_ALIAS, newAlias)
                            .putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context)));
                }).setNegativeButton(R.string.cancelButton, (dialog, whichButton) -> {
        }).show();
    }
}
