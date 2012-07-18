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

package li.klass.fhem.adapter.devices;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.*;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.TRXLightDevice;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.util.device.FloorplanUtil;

public class TRXLightAdapter extends ToggleableAdapter<TRXLightDevice> {

    public TRXLightAdapter() {
        super(TRXLightDevice.class);
    }

    @Override
    public void fillDeviceOverviewView(View view, final TRXLightDevice device) {
        TableLayout layout = (TableLayout) view.findViewById(R.id.device_overview_generic);
        layout.findViewById(R.id.deviceName).setVisibility(View.GONE);

        addOverviewSwitchActionRow(view.getContext(), device, layout);
    }

    @Override
    protected void afterPropertiesSet() {
        fieldNameAddedListeners.put("state", new FieldNameAddedToDetailListener<TRXLightDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, TRXLightDevice device, TableRow fieldTableRow) {
                    addDetailSwitchActionRow(context, device, tableLayout);
            }
        });

        detailActions.add(new DeviceDetailViewAction<TRXLightDevice>(R.string.switchSetOptions) {
            @Override
            public void onButtonClick(final Context context, final TRXLightDevice device) {
                showSwitchOptionsMenu(context, device);
            }
        });
    }

    private void showSwitchOptionsMenu(final Context context, final TRXLightDevice device) {
        AlertDialog.Builder contextMenu = new AlertDialog.Builder(context);
        contextMenu.setTitle(context.getResources().getString(R.string.switchDevice));
        final String[] setOptions = device.getAvailableTargetStates();

        contextMenu.setItems(setOptions, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                final String option = setOptions[item];

                if (option.equals("off-for-timer") || option.equals("on-for-timer")) {
                    final EditText input = new EditText(context);
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.howLong)
                            .setView(input)
                            .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String time = input.getText().toString();
                                    try {
                                        switchDeviceState(option + " " + time, device, context);
                                    } catch (NumberFormatException e) {
                                        Toast.makeText(context, R.string.notNumericError, Toast.LENGTH_SHORT).show();
                                    }
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

    private void switchDeviceState(String newState, TRXLightDevice device, final Context context) {
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

    @Override
    protected void fillFloorplanView(final Context context, final TRXLightDevice device, LinearLayout layout, FloorplanViewSettings viewSettings) {
        ImageView buttonView = FloorplanUtil.createSwitchStateBasedImageView(context, device);
        layout.addView(buttonView);
    }
}
