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

package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.TableLayout;
import android.widget.TableRow;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DimmableAdapter;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.genericui.ButtonActionRow;
import li.klass.fhem.adapter.devices.genericui.SeekBarActionRowFullWidth;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.EnOceanDevice;
import li.klass.fhem.service.intent.DeviceIntentService;

public class EnOceanAdapter extends DimmableAdapter<EnOceanDevice> {
    public EnOceanAdapter() {
        super(EnOceanDevice.class);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        registerFieldListener("state", new FieldNameAddedToDetailListener<EnOceanDevice>() {
            @Override
            protected void onFieldNameAdded(final Context context, TableLayout tableLayout,
                                            String field, final EnOceanDevice device,
                                            TableRow fieldTableRow) {

                tableLayout.addView(new ButtonActionRow(device.getEventMapStateFor("stop")) {

                    @Override
                    protected void onButtonClick() {
                        Intent intent = new Intent(Actions.DEVICE_SET_STATE);
                        intent.setClass(context, DeviceIntentService.class);
                        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                        intent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, "stop");
                        context.startService(intent);
                    }
                }.createRow(getInflater()));
            }

            @Override
            public boolean supportsDevice(EnOceanDevice device) {
                return device.getSubType() == EnOceanDevice.SubType.SHUTTER;
            }
        });

        registerFieldListener("shutterPositionText", new FieldNameAddedToDetailListener<EnOceanDevice>() {
            @Override
            protected void onFieldNameAdded(Context context, TableLayout tableLayout, String field,
                                            EnOceanDevice device, TableRow fieldTableRow) {

                tableLayout.addView(new SeekBarActionRowFullWidth<EnOceanDevice>(
                        device.getShutterPosition(), 100, R.layout.device_detail_seekbarrow_full_width
                ) {
                    @Override
                    public void onStopTrackingTouch(final Context context, final EnOceanDevice device, final int progress) {
                        Intent intent = new Intent(Actions.DEVICE_SET_STATE);
                        intent.setClass(context, DeviceIntentService.class);
                        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                        intent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, "position " + progress);
                        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                if (resultCode != ResultCodes.SUCCESS) return;

                                device.setShutterPosition(progress);
                                context.sendBroadcast(new Intent(Actions.DO_UPDATE));
                            }
                        });

                        context.startService(intent);
                    }
                }.createRow(getInflater(), device));
            }


            @Override
            public boolean supportsDevice(EnOceanDevice device) {
                return device.getSubType() == EnOceanDevice.SubType.SHUTTER;
            }
        });
    }
}
