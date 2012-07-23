/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 *  server.
 *
 *  Copyright (c) 2012, Matthias Klass or third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by Red Hat Inc.
 *
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 *  for more details.
 *
 *  You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 *  along with this distribution; if not, write to:
 *    Free Software Foundation, Inc.
 *    51 Franklin Street, Fifth Floor
 */

package li.klass.fhem.adapter.devices.genericui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.EditText;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceStateAdditionalInformationType;
import li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation;
import li.klass.fhem.util.DialogUtil;

public class AvailableTargetStatesSwitchActionRow<D extends Device<D>> extends DeviceDetailViewAction<D> {
    public AvailableTargetStatesSwitchActionRow() {
        super(R.string.switchSetOptions);
    }

    @Override
    public void onButtonClick(Context context, D device) {
        showSwitchOptionsMenu(context, device);
    }

    private void showSwitchOptionsMenu(final Context context, final D device) {
        AlertDialog.Builder contextMenu = new AlertDialog.Builder(context);
        contextMenu.setTitle(context.getResources().getString(R.string.switchDevice));
        final String[] setOptions = device.getAvailableTargetStates();

        contextMenu.setItems(setOptions, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                final String option = setOptions[item];

                final DeviceStateRequiringAdditionalInformation specialDeviceState =
                        DeviceStateRequiringAdditionalInformation.deviceStateForFHEM(option);

                if (specialDeviceState != null) {
                    final EditText input = new EditText(context);
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.stateAppendix)
                            .setView(input)
                            .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String value = input.getText().toString();
                                    handleAdditionalInformationValue(value, specialDeviceState, option, device, context);
                                }
                            }).setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();
                } else {
                    switchDeviceState(option, device, context);
                }
                dialog.dismiss();
            }
        });
        contextMenu.show();
    }

    private void handleAdditionalInformationValue(String value, DeviceStateRequiringAdditionalInformation specialDeviceState,
                                                  String option, D device, Context context) {

        for (DeviceStateAdditionalInformationType type :specialDeviceState.getAdditionalInformationTypes()) {
            if (! type.matches(value)) {
                DialogUtil.showAlertDialog(context, R.string.error, R.string.invalidInput);
                return;
            }
        }

        switchDeviceState(option + " " + value, device, context);
    }

    private void switchDeviceState(String newState, D device, final Context context) {
        Intent intent = new Intent(Actions.DEVICE_SET_STATE);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, newState);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                context.sendBroadcast(new Intent(Actions.DO_UPDATE));
            }
        });
        context.startService(intent);
    }
}
