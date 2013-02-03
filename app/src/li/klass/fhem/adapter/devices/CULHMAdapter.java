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
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DimmableAdapter;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.genericui.CustomViewTableRow;
import li.klass.fhem.adapter.devices.genericui.HeatingModeListener;
import li.klass.fhem.adapter.devices.genericui.TemperatureChangeTableRow;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.widget.LitreContentView;

import static li.klass.fhem.domain.CULHMDevice.MAXIMUM_TEMPERATURE;
import static li.klass.fhem.domain.CULHMDevice.MINIMUM_TEMPERATURE;
import static li.klass.fhem.domain.CULHMDevice.SubType.HEATING;

public class CULHMAdapter extends DimmableAdapter<CULHMDevice> {

    public CULHMAdapter() {
        super(CULHMDevice.class);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();
        registerFieldListener("state", new FieldNameAddedToDetailListener<CULHMDevice>() {
            @Override
            public void onFieldNameAdded(final Context context, TableLayout tableLayout, String field, final CULHMDevice device, TableRow fieldTableRow) {
                switch (device.getSubType()) {
                    case KFM100:
                        tableLayout.addView(new CustomViewTableRow() {
                            @Override
                            public View getContentView() {
                                return new LitreContentView(context, device.getFillContentPercentageRaw());
                            }
                        }.createRow(inflater));
                        break;
                }
            }
        });

        registerFieldListener("state", new HeatingModeListener<CULHMDevice, CULHMDevice.HeatingMode>() {
            @Override
            protected boolean doAddField(CULHMDevice device) {
                return device.getHeatingMode() != null;
            }
        });

        registerFieldListener("desiredTempDesc", new FieldNameAddedToDetailListener<CULHMDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, CULHMDevice device, TableRow fieldTableRow) {
                if (device.getSubType() != HEATING) return;

                tableLayout.addView(new TemperatureChangeTableRow<CULHMDevice>(context, device.getDesiredTemp(), fieldTableRow,
                        Actions.DEVICE_SET_DESIRED_TEMPERATURE, R.string.desiredTemperature, MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE)
                        .createRow(inflater, device));
            }
        });
    }
}
