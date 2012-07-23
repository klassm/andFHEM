/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.core.GenericDeviceAdapter;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.adapter.devices.genericui.SeekBarActionRow;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.DummyDevice;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.util.DialogUtil;

import java.util.List;

public class DummyAdapter extends ToggleableAdapter<DummyDevice> {
    public DummyAdapter() {
        super(DummyDevice.class);
    }

    @Override
    protected void fillDeviceOverviewView(View view, DummyDevice device) {
        if (device.supportsToggle()) {
            TableLayout layout = (TableLayout) view.findViewById(R.id.device_overview_generic);
            layout.findViewById(R.id.deviceName).setVisibility(View.GONE);
            addOverviewSwitchActionRow(view.getContext(), device, layout);
        } else {
            super.fillDeviceOverviewView(view, device);
        }
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        detailActions.add(new DeviceDetailViewAction<DummyDevice>(R.string.set_value) {
            @Override
            public void onButtonClick(final Context context, final DummyDevice device) {
                String title = context.getString(R.string.set_value);
                DialogUtil.showInputBox(context, title, device.getState(), new DialogUtil.InputDialogListener() {
                    @Override
                    public void onClick(String text) {
                        Intent intent = new Intent(Actions.DEVICE_SET_STATE);
                        intent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, text);
                        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                if (resultCode == ResultCodes.SUCCESS) {
                                    context.sendBroadcast(new Intent(Actions.DO_UPDATE));
                                }
                            }
                        });
                        context.startService(intent);
                    }
                });
            }
        });

        fieldNameAddedListeners.put("state", new FieldNameAddedToDetailListener<DummyDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, DummyDevice device, TableRow fieldTableRow) {
                if (device.supportsToggle()) {
                    addDetailSwitchActionRow(context, device, tableLayout);
                }
            }
        });

    }
}
