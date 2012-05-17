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
import android.view.View;
import android.widget.TableLayout;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.generic.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.generic.GenericDeviceAdapter;
import li.klass.fhem.adapter.devices.generic.ToggleActionRow;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.SISPMSDevice;

import static li.klass.fhem.adapter.devices.generic.ToggleActionRow.LAYOUT_DETAIL;
import static li.klass.fhem.adapter.devices.generic.ToggleActionRow.LAYOUT_OVERVIEW;

public class SISPMSAdapter extends GenericDeviceAdapter<SISPMSDevice> {

    public SISPMSAdapter() {
        super(SISPMSDevice.class);
    }

    private class TableRow extends ToggleActionRow<SISPMSDevice> {

        public TableRow(SISPMSDevice device, int layout) {
            super(device.getAliasOrName(), layout, device.isOn());
        }

        @Override
        public void onButtonClick(Context context, SISPMSDevice device) {
            Intent intent = new Intent(Actions.DEVICE_TOGGLE_STATE);
            intent.putExtras(new Bundle());
            intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
            AndFHEMApplication.getContext().startService(intent);
        }
    }

    @Override
    public void fillDeviceOverviewView(View view, final SISPMSDevice device) {
        TableLayout layout = (TableLayout) view.findViewById(R.id.device_overview_generic);
        layout.findViewById(R.id.deviceName).setVisibility(View.GONE);
        layout.addView(new TableRow(device, LAYOUT_OVERVIEW).createRow(view.getContext(), inflater, device));
    }

    @Override
    protected void afterPropertiesSet() {
        fieldNameAddedListeners.put("state", new FieldNameAddedToDetailListener<SISPMSDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, SISPMSDevice device, android.widget.TableRow fieldTableRow) {
                tableLayout.addView(new TableRow(device, LAYOUT_DETAIL)
                        .createRow(tableLayout.getContext(), inflater, device));
            }
        });
    }
}
