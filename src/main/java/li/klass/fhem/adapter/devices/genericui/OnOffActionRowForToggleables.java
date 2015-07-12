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

package li.klass.fhem.adapter.devices.genericui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;

import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.ToggleableDevice;

public class OnOffActionRowForToggleables extends OnOffActionRow {

    public OnOffActionRowForToggleables(int layoutId) {
        super(layoutId);
    }

    public TableRow createRow(LayoutInflater inflater, final FhemDevice device, Context context) {
        TableRow tableRow = super.createRow(inflater, device, context);

        Button onButton = findOnButton(tableRow);
        Button offButton = findOffButton(tableRow);

        ToggleableDevice toggleableDevice = getToggleableDevice(device);
        switch (toggleableDevice.getButtonHookType()) {
            case ON_DEVICE:
                offButton.setVisibility(View.GONE);
                onButton.setVisibility(View.VISIBLE);
                break;
            case OFF_DEVICE:
                onButton.setVisibility(View.GONE);
                offButton.setVisibility(View.VISIBLE);
                break;
        }

        return tableRow;
    }

    private ToggleableDevice getToggleableDevice(FhemDevice device) {
        return (ToggleableDevice) device;
    }

    @Override
    protected String getOnStateName(FhemDevice device, Context context) {
        return getToggleableDevice(device).getOnStateName();
    }

    @Override
    protected String getOffStateName(FhemDevice device, Context context) {
        return getToggleableDevice(device).getOffStateName();
    }

    @Override
    protected boolean isOn(FhemDevice device) {
        return getToggleableDevice(device).isOnRespectingInvertHook();
    }
}
