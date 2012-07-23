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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesSwitchActionRow;
import li.klass.fhem.adapter.devices.genericui.SeekBarActionRow;
import li.klass.fhem.adapter.devices.genericui.UpDownButtonRow;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.util.device.FloorplanUtil;

public class FS20Adapter extends ToggleableAdapter<FS20Device> {

    public FS20Adapter() {
        super(FS20Device.class);
    }

    private class FS20DimUpDownRow extends UpDownButtonRow<FS20Device> {

        public FS20DimUpDownRow() {
            super("");
        }

        @Override
        public void onUpButtonClick(Context context, FS20Device device) {
            sendTargetDimState(context, device, device.getDimUpProgress());
        }

        @Override
        public void onDownButtonClick(Context context, FS20Device device) {
            sendTargetDimState(context, device, device.getDimDownProgress());
        }

        private void sendTargetDimState(final Context context, FS20Device device, int target) {

            Intent intent = new Intent(Actions.DEVICE_DIM);
            intent.putExtra(BundleExtraKeys.DEVICE_DIM_PROGRESS, target);
            intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
            intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    context.sendBroadcast(new Intent(Actions.DO_UPDATE));
                }
            });

            context.startService(intent);
        }
    }

    @Override
    public void fillDeviceOverviewView(View view, final FS20Device device) {
        TableLayout layout = (TableLayout) view.findViewById(R.id.device_overview_generic);
        layout.findViewById(R.id.deviceName).setVisibility(View.GONE);

        if (device.isDimDevice()) {
            layout.addView(new SeekBarActionRow<FS20Device>(device.getFS20DimState(), device.getAliasOrName(), SeekBarActionRow.LAYOUT_OVERVIEW)
                    .createRow(inflater, device));
        } else {
            addOverviewSwitchActionRow(view.getContext(), device, layout);
        }
    }

    @Override
    protected void afterPropertiesSet() {
        fieldNameAddedListeners.put("state", new FieldNameAddedToDetailListener<FS20Device>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FS20Device device, TableRow fieldTableRow) {
                if (device.isDimDevice()) {
                    tableLayout.addView(new SeekBarActionRow<FS20Device>(device.getFS20DimState(), R.string.blank, SeekBarActionRow.LAYOUT_DETAIL)
                            .createRow(inflater, device));
                    tableLayout.addView(new FS20DimUpDownRow()
                            .createRow(context, inflater, device));
                } else {
                    addDetailSwitchActionRow(context, device, tableLayout);
                }
            }
        });

        detailActions.add(new AvailableTargetStatesSwitchActionRow<FS20Device>());
    }

    @Override
    protected void fillFloorplanView(final Context context, final FS20Device device, LinearLayout layout, FloorplanViewSettings viewSettings) {
        ImageView buttonView = FloorplanUtil.createSwitchStateBasedImageView(context, device);
        layout.addView(buttonView);
    }
}
