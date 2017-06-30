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

package li.klass.fhem.adapter.devices.core;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.DimmableDeviceDimActionRowFullWidth;
import li.klass.fhem.adapter.devices.genericui.UpDownButtonRow;
import li.klass.fhem.adapter.devices.strategy.DimmableStrategy;
import li.klass.fhem.adapter.devices.strategy.ViewStrategy;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.core.FhemDevice;

import static li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener.NotificationDeviceType.DIMMER;

public class DimmableAdapter extends ToggleableAdapter {

    @Inject
    StateUiService stateUiService;

    @Inject
    DimmableStrategy dimmableStrategy;


    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        registerFieldListener("state", new FieldNameAddedToDetailListener(DIMMER) {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, String connectionId, TableRow fieldTableRow) {
                LayoutInflater inflater = LayoutInflater.from(context);
                tableLayout.addView(new DimmableDeviceDimActionRowFullWidth(device, R.layout.device_detail_seekbarrow_full_width, fieldTableRow)
                        .createRow(inflater, device));
                tableLayout.addView(new DimUpDownRow(stateUiService)
                        .createRow(context, inflater, device));
            }

            @Override
            public boolean supportsDevice(FhemDevice device) {
                return ((DimmableDevice) device).supportsDim();
            }
        });
    }

    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return DimmableDevice.class;
    }

    public static class DimUpDownRow extends UpDownButtonRow {

        private final StateUiService stateUiService;

        public DimUpDownRow(StateUiService stateUiService) {
            super("");
            this.stateUiService = stateUiService;
        }

        @Override
        public void onUpButtonClick(Context context, FhemDevice device) {
            DimmableDevice dimmableDevice = (DimmableDevice) device;
            dim(context, dimmableDevice, dimmableDevice.getDimUpPosition());
        }

        @Override
        public void onDownButtonClick(Context context, FhemDevice device) {
            DimmableDevice dimmableDevice = (DimmableDevice) device;
            dim(context, dimmableDevice, dimmableDevice.getDimDownPosition());
        }

        protected void dim(Context context, DimmableDevice device, float newPosition) {
            float currentPosition = device.getDimPosition();
            if (currentPosition != newPosition) {
                stateUiService.setDim(device, newPosition, context);
            }
        }
    }

    @Override
    public Class getOverviewViewHolderClass() {
        return GenericDeviceOverviewViewHolder.class;
    }

    @Override
    protected void fillOverviewStrategies(List<ViewStrategy> overviewStrategies) {
        super.fillOverviewStrategies(overviewStrategies);
        overviewStrategies.add(dimmableStrategy);
    }
}
