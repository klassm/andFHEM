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
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.common.collect.Lists;

import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.core.GenericDeviceAdapter;
import li.klass.fhem.adapter.devices.genericui.StateChangingSeekBarFullWidth;
import li.klass.fhem.adapter.devices.genericui.ToggleActionRow;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.PCA9532Device;
import li.klass.fhem.domain.setlist.SetListSliderValue;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.util.ApplicationProperties;

import static java.util.Collections.sort;

public class PCA9532DeviceAdapter extends GenericDeviceAdapter<PCA9532Device> {
    @Inject
    ApplicationProperties applicationProperties;

    public PCA9532DeviceAdapter() {
        super(PCA9532Device.class);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        registerFieldListener("state", new FieldNameAddedToDetailListener<PCA9532Device>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, PCA9532Device device, TableRow fieldTableRow) {
                List<String> ports = Lists.newArrayList(device.getPortsIsOnMap().keySet());
                sort(ports);

                for (String port : ports) {
                    createPortRow(context, tableLayout, port, device);
                }
            }

            private void createPortRow(Context context, TableLayout tableLayout, final String port, PCA9532Device device) {
                tableLayout.addView(new ToggleActionRow<PCA9532Device>(port, ToggleActionRow.LAYOUT_DETAIL) {

                    @Override
                    protected boolean isOn(PCA9532Device device) {
                        return device.getPortsIsOnMap().get(port);
                    }

                    @Override
                    protected void onButtonClick(final Context context, final PCA9532Device device, final boolean isChecked) {
                        Intent intent = new Intent(Actions.DEVICE_SET_SUB_STATE);
                        intent.setClass(context, DeviceIntentService.class);
                        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                        intent.putExtra(BundleExtraKeys.STATE_NAME, port);
                        intent.putExtra(BundleExtraKeys.STATE_VALUE, isChecked ? "on" : "off");
                        putUpdateExtra(intent);
                        context.startService(intent);
                    }
                }.createRow(context, LayoutInflater.from(context), device));
            }
        });

        registerFieldListener("pwm0", new FieldNameAddedToDetailListener<PCA9532Device>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, PCA9532Device device, TableRow fieldTableRow) {
                tableLayout.addView(new StateChangingSeekBarFullWidth<PCA9532Device>(
                        context, device.getPwm0(),
                        (SetListSliderValue) device.getSetList().get("PWM0"), "PWM0", applicationProperties)
                        .createRow(getInflater(), device));
            }
        });

        registerFieldListener("pwm1", new FieldNameAddedToDetailListener<PCA9532Device>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, PCA9532Device device, TableRow fieldTableRow) {
                tableLayout.addView(new StateChangingSeekBarFullWidth<PCA9532Device>(
                        context, device.getPwm1(),
                        (SetListSliderValue) device.getSetList().get("PWM1"), "PWM1", applicationProperties)
                        .createRow(getInflater(), device));
            }
        });
    }
}
