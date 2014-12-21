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
import li.klass.fhem.adapter.devices.core.GenericDeviceAdapter;
import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewButtonAction;
import li.klass.fhem.adapter.devices.genericui.HeatingModeListener;
import li.klass.fhem.adapter.devices.genericui.TemperatureChangeTableRow;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.MaxDevice;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.util.ApplicationProperties;

import static li.klass.fhem.domain.FHTDevice.MAXIMUM_TEMPERATURE;
import static li.klass.fhem.domain.FHTDevice.MINIMUM_TEMPERATURE;
import static li.klass.fhem.domain.MaxDevice.HeatingMode;

public class MaxAdapter extends GenericDeviceAdapter<MaxDevice> {
    @Inject
    ApplicationProperties applicationProperties;

    public MaxAdapter() {
        super(MaxDevice.class);
    }

    @Override
    protected void afterPropertiesSet() {
        registerFieldListener("state", new HeatingModeListener<MaxDevice, HeatingMode>() {
            @Override
            protected boolean doAddField(MaxDevice device) {
                return device.getSubType() == MaxDevice.SubType.TEMPERATURE;
            }
        });

        registerFieldListener("desiredTempDesc", new FieldNameAddedToDetailListener<MaxDevice>() {
            @Override
            public void onFieldNameAdded(final Context context, TableLayout tableLayout, String field, MaxDevice device, TableRow fieldTableRow) {
                if (device.getSubType() != MaxDevice.SubType.TEMPERATURE) return;
                if (device.getHeatingMode() != HeatingMode.MANUAL) return;

                tableLayout.addView(new TemperatureChangeTableRow<MaxDevice>(context, device.getDesiredTemp(), fieldTableRow,
                        Actions.DEVICE_SET_DESIRED_TEMPERATURE, R.string.desiredTemperature,
                        MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE, applicationProperties) {
                    @Override
                    protected void onIntentCreation(Intent intent) {
                        putUpdateIntent(intent);
                    }

                    @Override
                    protected ApplicationProperties getApplicationProperties() {
                        return applicationProperties;
                    }
                }
                        .createRow(getInflater(), device));
            }
        });

        registerFieldListener("windowOpenTemp", new FieldNameAddedToDetailListener<MaxDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, MaxDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new TemperatureChangeTableRow<MaxDevice>(context, device.getWindowOpenTemp(), fieldTableRow,
                        Actions.DEVICE_SET_WINDOW_OPEN_TEMPERATURE, R.string.windowOpenTemp,
                        MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE, applicationProperties)
                        .createRow(getInflater(), device));
            }
        });

        registerFieldListener("ecoTemp", new FieldNameAddedToDetailListener<MaxDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, MaxDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new TemperatureChangeTableRow<MaxDevice>(context, device.getEcoTemp(), fieldTableRow,
                        Actions.DEVICE_SET_ECO_TEMPERATURE, R.string.ecoTemperature,
                        MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE, applicationProperties)
                        .createRow(getInflater(), device));
            }
        });

        registerFieldListener("comfortTemp", new FieldNameAddedToDetailListener<MaxDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, MaxDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new TemperatureChangeTableRow<MaxDevice>(context, device.getComfortTemp(), fieldTableRow,
                        Actions.DEVICE_SET_COMFORT_TEMPERATURE, R.string.comfortTemperature,
                        MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE, applicationProperties)
                        .createRow(getInflater(), device));
            }
        });

        detailActions.add(new DeviceDetailViewButtonAction<MaxDevice>(R.string.timetable) {
            @Override
            public void onButtonClick(Context context, MaxDevice device) {
                Intent intent = new Intent(Actions.SHOW_FRAGMENT);
                intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.INTERVAL_WEEK_PROFILE);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                context.sendBroadcast(intent);
            }

            @Override
            public boolean isVisible(MaxDevice device) {
                return device.getSubType() == MaxDevice.SubType.TEMPERATURE;
            }
        });
    }

    private void putUpdateIntent(Intent intent) {
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(getContext()));
    }
}
