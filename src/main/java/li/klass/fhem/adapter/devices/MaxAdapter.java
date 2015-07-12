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

import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.ExplicitOverviewDetailDeviceAdapter;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewButtonAction;
import li.klass.fhem.adapter.devices.genericui.HeatingModeListener;
import li.klass.fhem.adapter.devices.genericui.TemperatureChangeTableRow;
import li.klass.fhem.adapter.uiservice.FragmentUiService;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.MaxDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.util.ApplicationProperties;

import static li.klass.fhem.domain.FHTDevice.MAXIMUM_TEMPERATURE;
import static li.klass.fhem.domain.FHTDevice.MINIMUM_TEMPERATURE;
import static li.klass.fhem.domain.MaxDevice.HeatingMode;

public class MaxAdapter extends ExplicitOverviewDetailDeviceAdapter {
    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    FragmentUiService fragmentUiService;

    public MaxAdapter() {
        super();
    }

    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return MaxDevice.class;
    }

    @Override
    protected void inject(ApplicationComponent daggerComponent) {
        daggerComponent.inject(this);
    }

    @Override
    protected void afterPropertiesSet() {
        registerFieldListener("state", new HeatingModeListener<MaxDevice, HeatingMode>() {
            @Override
            protected boolean doAddField(FhemDevice device) {
                return ((MaxDevice) device).getSubType() == MaxDevice.SubType.TEMPERATURE;
            }
        });

        registerFieldListener("desiredTempDesc", new FieldNameAddedToDetailListener() {
            @Override
            public void onFieldNameAdded(final Context context, TableLayout tableLayout, String field, FhemDevice device, TableRow fieldTableRow) {
                MaxDevice maxDevice = (MaxDevice) device;
                if (maxDevice.getSubType() != MaxDevice.SubType.TEMPERATURE) return;
                if (maxDevice.getHeatingMode() != HeatingMode.MANUAL) return;

                tableLayout.addView(new TemperatureChangeTableRow(context, maxDevice.getDesiredTemp(), fieldTableRow,
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

        registerFieldListener("windowOpenTemp", new FieldNameAddedToDetailListener() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new TemperatureChangeTableRow(context, ((MaxDevice) device).getWindowOpenTemp(), fieldTableRow,
                        Actions.DEVICE_SET_WINDOW_OPEN_TEMPERATURE, R.string.windowOpenTemp,
                        MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE, applicationProperties)
                        .createRow(getInflater(), device));
            }
        });

        registerFieldListener("ecoTemp", new FieldNameAddedToDetailListener() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new TemperatureChangeTableRow(context, ((MaxDevice) device).getEcoTemp(), fieldTableRow,
                        Actions.DEVICE_SET_ECO_TEMPERATURE, R.string.ecoTemperature,
                        MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE, applicationProperties)
                        .createRow(getInflater(), device));
            }
        });

        registerFieldListener("comfortTemp", new FieldNameAddedToDetailListener() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new TemperatureChangeTableRow(context, ((MaxDevice) device).getComfortTemp(), fieldTableRow,
                        Actions.DEVICE_SET_COMFORT_TEMPERATURE, R.string.comfortTemperature,
                        MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE, applicationProperties)
                        .createRow(getInflater(), device));
            }
        });
    }

    @Override
    protected List<DeviceDetailViewAction> provideDetailActions() {
        List<DeviceDetailViewAction> detailActions = super.provideDetailActions();

        detailActions.add(new DeviceDetailViewButtonAction(R.string.timetable) {
            @Override
            public void onButtonClick(Context context, FhemDevice device) {
                fragmentUiService.showIntervalWeekProfileFor(device, context);
            }

            @Override
            public boolean isVisible(FhemDevice device) {
                return ((MaxDevice) device).getSubType() == MaxDevice.SubType.TEMPERATURE;
            }
        });

        return detailActions;
    }

    private void putUpdateIntent(Intent intent) {
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(getContext()));
    }
}
