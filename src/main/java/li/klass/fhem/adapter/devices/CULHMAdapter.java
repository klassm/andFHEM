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
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DimmableAdapter;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.genericui.CustomViewTableRow;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewButtonAction;
import li.klass.fhem.adapter.devices.genericui.HeatingModeListener;
import li.klass.fhem.adapter.devices.genericui.TemperatureChangeTableRow;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.widget.LitreContentView;

import static li.klass.fhem.constants.Actions.DEVICE_SET_DESIRED_TEMPERATURE;
import static li.klass.fhem.constants.Actions.SHOW_FRAGMENT;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.FRAGMENT;
import static li.klass.fhem.domain.CULHMDevice.MAXIMUM_TEMPERATURE;
import static li.klass.fhem.domain.CULHMDevice.MINIMUM_TEMPERATURE;
import static li.klass.fhem.domain.CULHMDevice.SubType.THERMOSTAT;

public class CULHMAdapter extends DimmableAdapter {
    @Inject
    ApplicationProperties applicationProperties;

    public CULHMAdapter() {
        super();
    }

    @Override
    protected void inject(ApplicationComponent daggerComponent) {
        daggerComponent.inject(this);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();
        registerFieldListener("state", new FieldNameAddedToDetailListener() {
            @Override
            public void onFieldNameAdded(final Context context, final TableLayout tableLayout, String field, final FhemDevice device, TableRow fieldTableRow) {
                final CULHMDevice culhmDevice = (CULHMDevice) device;
                switch (culhmDevice.getSubType()) {
                    case FILL_STATE:
                        tableLayout.addView(new CustomViewTableRow() {
                            @Override
                            public View getContentView() {
                                return new LitreContentView(context, culhmDevice.getFillContentPercentageRaw());
                            }
                        }.createRow(getInflater(), tableLayout));
                        break;
                }
            }
        });

        registerFieldListener("state", new HeatingModeListener<CULHMDevice, CULHMDevice.HeatingMode>() {
            @Override
            protected boolean doAddField(FhemDevice device) {
                return ((CULHMDevice) device).getSubType() == THERMOSTAT;
            }

            @Override
            protected CULHMDevice.HeatingMode getUnknownMode() {
                return CULHMDevice.HeatingMode.UNKNOWN;
            }
        });

        registerFieldListener("desiredTempDesc", new FieldNameAddedToDetailListener() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, TableRow fieldTableRow) {
                CULHMDevice culhmDevice = (CULHMDevice) device;
                if (culhmDevice.getSubType() != THERMOSTAT) return;

                tableLayout.addView(new TemperatureChangeTableRow(context, culhmDevice.getDesiredTemp(), fieldTableRow,
                        DEVICE_SET_DESIRED_TEMPERATURE, R.string.desiredTemperature,
                        MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE, applicationProperties)
                        .createRow(getInflater(), device));
            }
        });

        registerFieldListener("commandAccepted", new FieldNameAddedToDetailListener() {
            @Override
            protected void onFieldNameAdded(Context context, TableLayout tableLayout, String field,
                                            FhemDevice device, TableRow fieldTableRow) {
                if (!((CULHMDevice) device).isLastCommandAccepted()) {
                    TextView valueView = (TextView) fieldTableRow.findViewById(R.id.value);
                    valueView.setTextColor(context.getResources().getColor(R.color.red));
                }
            }
        });
    }

    @Override
    protected boolean isOverviewError(FhemDevice device, long lastUpdate) {
        return super.isOverviewError(device, lastUpdate) || !((CULHMDevice) device).isLastCommandAccepted();
    }

    @Override
    protected List<DeviceDetailViewAction> provideDetailActions() {
        List<DeviceDetailViewAction> detailActions = super.provideDetailActions();

        detailActions.add(new DeviceDetailViewButtonAction(R.string.timetable) {
            @Override
            public void onButtonClick(Context context, FhemDevice device) {
                context.sendBroadcast(
                        new Intent(SHOW_FRAGMENT)
                                .putExtra(FRAGMENT, FragmentType.INTERVAL_WEEK_PROFILE)
                                .putExtra(DEVICE_NAME, device.getName()));
            }

            @Override
            public boolean isVisible(FhemDevice device) {
                return ((CULHMDevice) device).getSubType() == THERMOSTAT;
            }
        });

        return detailActions;
    }
}
