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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.domain.FS20ZDRDevice;

public class FS20ZDRDeviceAdapter extends ToggleableAdapterWithSwitchActionRow<FS20ZDRDevice> {
    public FS20ZDRDeviceAdapter() {
        super(FS20ZDRDevice.class);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        detailActions.add(new DeviceDetailViewAction<FS20ZDRDevice>() {
            @Override
            public View createView(Context context, LayoutInflater inflater, FS20ZDRDevice device, LinearLayout parent) {
                View view = inflater.inflate(R.layout.fs20_zdr_actions, parent, false);

                registerActionHandlerFor(context, view, device, R.id.vol_up, "volume_up");
                registerActionHandlerFor(context, view, device, R.id.vol_down, "volume_down");
                registerActionHandlerFor(context, view, device, R.id.left, "left");
                registerActionHandlerFor(context, view, device, R.id.right, "right");
                registerActionHandlerFor(context, view, device, R.id.slp, "sleep");
                registerActionHandlerFor(context, view, device, R.id.ms, "ms");
                registerActionHandlerFor(context, view, device, R.id.prog_1, "1");
                registerActionHandlerFor(context, view, device, R.id.prog_2, "2");
                registerActionHandlerFor(context, view, device, R.id.prog_3, "3");
                registerActionHandlerFor(context, view, device, R.id.prog_4, "4");
                registerActionHandlerFor(context, view, device, R.id.prog_5, "5");
                registerActionHandlerFor(context, view, device, R.id.prog_6, "6");
                registerActionHandlerFor(context, view, device, R.id.prog_7, "7");
                registerActionHandlerFor(context, view, device, R.id.prog_8, "8");

                return view;
            }
        });
    }

    private void registerActionHandlerFor(final Context context, View view, final FS20ZDRDevice device,
                                          int buttonId, final String state) {
        Button button = (Button) view.findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendStateAction(context, device, state);
            }
        });
    }
}
