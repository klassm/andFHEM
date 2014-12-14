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
import android.widget.TableLayout;
import android.widget.TableRow;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DimmableAdapter;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.genericui.ColorPickerRow;
import li.klass.fhem.adapter.devices.genericui.StateChangingSeekBarFullWidth;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.HUEDevice;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.StringUtil;

public class HueDeviceAdapter extends DimmableAdapter<HUEDevice> {
    @Inject
    ApplicationProperties applicationProperties;

    public HueDeviceAdapter() {
        super(HUEDevice.class);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        registerFieldListener("saturationDesc", new FieldNameAddedToDetailListener<HUEDevice>() {
            @Override
            public boolean supportsDevice(HUEDevice device) {
                return device.getSaturationDesc() != null;
            }

            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, HUEDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new StateChangingSeekBarFullWidth<HUEDevice>(context,
                        device.getSaturation(), 254, "sat", applicationProperties)
                        .createRow(getInflater(), device));

            }
        });

        registerFieldListener("rgbDesc", new FieldNameAddedToDetailListener<HUEDevice>() {
            @Override
            public void onFieldNameAdded(final Context context, TableLayout tableLayout, String field,
                                         final HUEDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new ColorPickerRow(device.getRgb(), R.string.hue) {
                    @Override
                    public void onColorChange(int color) {
                        String targetHexString = StringUtil.prefixPad(
                                Integer.toHexString(color),
                                "0", 6
                        );

                        Intent intent = new Intent(Actions.DEVICE_SET_SUB_STATE);
                        intent.setClass(context, DeviceIntentService.class);
                        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                        intent.putExtra(BundleExtraKeys.STATE_NAME, "rgb");
                        intent.putExtra(BundleExtraKeys.STATE_VALUE, targetHexString);
                        putUpdateExtra(intent);

                        context.startService(intent);
                    }
                }.createRow(context, getInflater(), tableLayout));
            }
        });
    }
}
