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

package li.klass.fhem.adapter.devices.core;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.ToggleableAdapterWithSwitchActionRow;
import li.klass.fhem.adapter.devices.genericui.DimActionRow;
import li.klass.fhem.adapter.devices.genericui.DimmableDeviceDimActionRowFullWidth;
import li.klass.fhem.adapter.devices.genericui.UpDownButtonRow;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.service.intent.DeviceIntentService;

import static li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener.NotificationDeviceType.DIMMER;

public class DimmableAdapter<D extends DimmableDevice<D>> extends ToggleableAdapterWithSwitchActionRow<D> {

    public DimmableAdapter(Class<D> deviceClass) {
        super(deviceClass);
    }

    @Override
    public void fillDeviceOverviewView(View view, final D device, long lastUpdate) {
        if (!device.supportsDim() || device.isSpecialButtonDevice()) {
            super.fillDeviceOverviewView(view, device, lastUpdate);
            return;
        }

        TableLayout layout = (TableLayout) view.findViewById(R.id.device_overview_generic);
        layout.findViewById(R.id.deviceName).setVisibility(View.GONE);

        layout.addView(new DimActionRow<D>(device.getAliasOrName(), DimActionRow.LAYOUT_OVERVIEW)
                .createRow(getInflater(), device));
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        registerFieldListener("state", new FieldNameAddedToDetailListener<D>(DIMMER) {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, D device, TableRow fieldTableRow) {
                tableLayout.addView(new DimmableDeviceDimActionRowFullWidth<D>(device, R.layout.device_detail_seekbarrow_full_width, fieldTableRow)
                        .createRow(getInflater(), device));
                tableLayout.addView(new DimUpDownRow()
                        .createRow(context, getInflater(), device));
            }

            @Override
            public boolean supportsDevice(D device) {
                return device.supportsDim();
            }
        });
    }

    private class DimUpDownRow extends UpDownButtonRow<D> {

        public DimUpDownRow() {
            super("");
        }

        @Override
        public void onUpButtonClick(Context context, D device) {
            sendTargetDimState(context, device, device.getDimUpPosition());
        }

        private void sendTargetDimState(final Context context, D device, int target) {

            Intent intent = new Intent(Actions.DEVICE_DIM);
            intent.setClass(context, DeviceIntentService.class);
            intent.putExtra(BundleExtraKeys.DEVICE_DIM_PROGRESS, target);
            intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
            intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context));

            context.startService(intent);
        }

        @Override
        public void onDownButtonClick(Context context, D device) {
            sendTargetDimState(context, device, device.getDimDownPosition());
        }
    }
}
