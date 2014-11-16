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
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.genericui.SpinnerActionRow;
import li.klass.fhem.adapter.devices.genericui.StateChangingSeekBarFullWidth;
import li.klass.fhem.adapter.devices.genericui.YesNoToggleDeviceActionRow;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.YamahaAVRDevice;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.util.ApplicationProperties;

public class YamahaAVRAdapter extends ToggleableAdapterWithSwitchActionRow<YamahaAVRDevice> {
    @Inject
    ApplicationProperties applicationProperties;

    public YamahaAVRAdapter() {
        super(YamahaAVRDevice.class);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        registerFieldListener("volumeDesc", new FieldNameAddedToDetailListener<YamahaAVRDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, YamahaAVRDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new StateChangingSeekBarFullWidth<YamahaAVRDevice>(context,
                        device.getVolume(), -80, 16, "volume", applicationProperties)
                        .createRow(getInflater(), device));
            }
        });

        registerFieldListener("state", new FieldNameAddedToDetailListener<YamahaAVRDevice>() {
            @Override
            protected void onFieldNameAdded(Context context, TableLayout tableLayout, String field, YamahaAVRDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new YesNoToggleDeviceActionRow<YamahaAVRDevice>(context, "mute", R.string.musicMute) {

                    @Override
                    public boolean isOn(YamahaAVRDevice device) {
                        return device.isMuted();
                    }
                }.createRow(context, getInflater(), device));
            }
        });

        registerFieldListener("state", new FieldNameAddedToDetailListener<YamahaAVRDevice>() {
            @Override
            protected void onFieldNameAdded(Context context, TableLayout tableLayout, String field, YamahaAVRDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new SpinnerActionRow<YamahaAVRDevice>(context, R.string.input, R.string.input,
                        YamahaAVRDevice.AVAILABLE_INPUTS, device.getSelectedInputPosition()) {

                    @Override
                    public void onItemSelected(Context context, YamahaAVRDevice device, String item) {
                        Intent intent = new Intent(Actions.DEVICE_SET_SUB_STATE);
                        intent.setClass(context, DeviceIntentService.class);
                        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                        intent.putExtra(BundleExtraKeys.STATE_NAME, "input");
                        intent.putExtra(BundleExtraKeys.STATE_VALUE, item);
                        putUpdateExtra(intent);

                        context.startService(intent);
                    }
                }.createRow(device, tableLayout));
            }
        });
    }
}
