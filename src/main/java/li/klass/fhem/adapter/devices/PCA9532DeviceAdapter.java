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
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.adapter.devices.core.ExplicitOverviewDetailDeviceAdapter;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.genericui.StateChangingSeekBarFullWidth;
import li.klass.fhem.adapter.devices.genericui.ToggleActionRow;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.behavior.dim.DimmableBehavior;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.PCA9532Device;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.util.ApplicationProperties;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.sort;

public class PCA9532DeviceAdapter extends ExplicitOverviewDetailDeviceAdapter {
    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    StateUiService stateUiService;

    public PCA9532DeviceAdapter() {
        super();
    }

    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return PCA9532Device.class;
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
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, TableRow fieldTableRow) {
                List<String> ports = newArrayList(((PCA9532Device) device).getPortsIsOnMap().keySet());
                sort(ports);

                for (String port : ports) {
                    createPortRow(context, tableLayout, port, ((PCA9532Device) device));
                }
            }

            private void createPortRow(Context context, TableLayout tableLayout, final String port, PCA9532Device device) {
                tableLayout.addView(new ToggleActionRow(LayoutInflater.from(context), ToggleActionRow.LAYOUT_DETAIL) {

                    @Override
                    protected boolean isOn(FhemDevice device) {
                        return ((PCA9532Device) device).getPortsIsOnMap().get(port);
                    }

                    @Override
                    protected void onButtonClick(final Context context, final FhemDevice device, final boolean isChecked) {
                        stateUiService.setSubState(device, port, isChecked ? "on" : "off", context);
                    }
                }.createRow(context, device, port));
            }
        });

        registerFieldListener("pwm0", new FieldNameAddedToDetailListener() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new StateChangingSeekBarFullWidth(
                        context, stateUiService, applicationProperties, DimmableBehavior.continuousBehaviorFor(device, "PWM0").get(), fieldTableRow)
                        .createRow(getInflater(), device));
            }
        });

        registerFieldListener("pwm1", new FieldNameAddedToDetailListener() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new StateChangingSeekBarFullWidth(
                        context, stateUiService, applicationProperties, DimmableBehavior.continuousBehaviorFor(device, "PWM1").get(), fieldTableRow)
                        .createRow(getInflater(), device));
            }
        });
    }
}
