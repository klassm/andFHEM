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
import android.view.LayoutInflater;
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
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.service.intent.DeviceIntentService;

import static li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener.NotificationDeviceType.DIMMER;

public class DimmableAdapter<D extends DimmableDevice<D>> extends ToggleableAdapterWithSwitchActionRow<D> {

    public DimmableAdapter(Class<D> deviceClass) {
        super(deviceClass);
    }

    @Override
    public View createOverviewView(LayoutInflater layoutInflater, View convertView, Device rawDevice, long lastUpdate) {
        D device = (D) rawDevice;
        if (!device.supportsDim() || device.isSpecialButtonDevice()) {
            return super.createOverviewView(layoutInflater, convertView, rawDevice, lastUpdate);
        }
        if(convertView == null || convertView.getTag() == null) {
            convertView = layoutInflater.inflate(R.layout.device_overview_generic, null);
            GenericDeviceOverviewViewHolder holder = new GenericDeviceOverviewViewHolder(convertView);
            convertView.setTag(holder);
        }
        GenericDeviceOverviewViewHolder holder = (GenericDeviceOverviewViewHolder) convertView.getTag();
        holder.resetHolder();
        holder.getDeviceName().setVisibility(View.GONE);
        DimActionRow<D> row = holder.getAdditionalHolderFor(DimActionRow.HOLDER_KEY);
        if(row == null) {
            row = new DimActionRow<>(layoutInflater);
            holder.putAdditionalHolder(DimActionRow.HOLDER_KEY, row);
        }
        row.fillWith(device, null);
        holder.getTableLayout().addView(row.getView());
        return convertView;
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        registerFieldListener("state", new FieldNameAddedToDetailListener<D>(DIMMER) {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, D device, TableRow fieldTableRow) {
                tableLayout.addView(new DimmableDeviceDimActionRowFullWidth<>(device, R.layout.device_detail_seekbarrow_full_width, fieldTableRow)
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

    @Override
    public Class getOverviewViewHolderClass() {
        return GenericDeviceOverviewViewHolder.class;
    }
}
