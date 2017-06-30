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

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.genericui.TemperatureChangeTableRow;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.PIDDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.util.ApplicationProperties;

import static li.klass.fhem.domain.PIDDevice.MAXIMUM_TEMPERATURE;
import static li.klass.fhem.domain.PIDDevice.MINIMUM_TEMPERATURE;

public class PIDDeviceAdapter extends ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow {
    @Inject
    ApplicationProperties applicationProperties;

    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return PIDDevice.class;
    }

    @Override
    protected void inject(ApplicationComponent daggerComponent) {
        daggerComponent.inject(this);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();
        registerFieldListener("desiredTemperature", new FieldNameAddedToDetailListener() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, String connectionId, TableRow fieldTableRow) {
                tableLayout.addView(new TemperatureChangeTableRow(context, ((PIDDevice) device).getDesiredTemp(), fieldTableRow,
                        Actions.DEVICE_SET_DESIRED_TEMPERATURE, R.string.desiredTemperature,
                        MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE, applicationProperties)
                        .createRow(LayoutInflater.from(context), device));
            }
        });
    }
}
