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
import android.widget.Toast;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.ToggleableAdapter;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.FS20ZDRDevice;
import li.klass.fhem.domain.core.FhemDevice;

public class FS20ZDRDeviceAdapter extends ToggleableAdapter {
    public FS20ZDRDeviceAdapter() {
        super();
    }

    @Override
    protected void inject(ApplicationComponent daggerComponent) {
        daggerComponent.inject(this);
    }

    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return FS20ZDRDevice.class;
    }

    @Override
    protected List<DeviceDetailViewAction> provideDetailActions() {
        List<DeviceDetailViewAction> detailActions = super.provideDetailActions();

        detailActions.add(new DeviceDetailViewAction() {
            @Override
            public View createView(Context context, LayoutInflater inflater, FhemDevice device, LinearLayout parent, String connectionId) {
                View view = inflater.inflate(R.layout.fs20_zdr_actions, parent, false);

                registerActionHandlerFor(context, view, device, R.id.vol_up, "volume_up", connectionId);
                registerActionHandlerFor(context, view, device, R.id.vol_down, "volume_down", connectionId);
                registerActionHandlerFor(context, view, device, R.id.left, "left", connectionId);
                registerActionHandlerFor(context, view, device, R.id.right, "right", connectionId);
                registerActionHandlerFor(context, view, device, R.id.slp, "sleep", connectionId);
                registerActionHandlerFor(context, view, device, R.id.ms, "ms", connectionId);
                registerActionHandlerFor(context, view, device, R.id.prog_1, "1", connectionId);
                registerActionHandlerFor(context, view, device, R.id.prog_2, "2", connectionId);
                registerActionHandlerFor(context, view, device, R.id.prog_3, "3", connectionId);
                registerActionHandlerFor(context, view, device, R.id.prog_4, "4", connectionId);
                registerActionHandlerFor(context, view, device, R.id.prog_5, "5", connectionId);
                registerActionHandlerFor(context, view, device, R.id.prog_6, "6", connectionId);
                registerActionHandlerFor(context, view, device, R.id.prog_7, "7", connectionId);
                registerActionHandlerFor(context, view, device, R.id.prog_8, "8", connectionId);

                return view;
            }
        });

        return detailActions;
    }

    private void registerActionHandlerFor(final Context context, View view, final FhemDevice device,
                                          int buttonId, final String state, final String connectionId) {
        Button button = (Button) view.findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stateUiService.setState(device, state, context, connectionId);
            }
        });
        if (NumberUtils.isNumber(state)) {
            button.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    stateUiService.setState(device, "program_" + state, context, connectionId);
                    Toast.makeText(context, String.format(context.getString(R.string.programChannelSuccess), state), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }
}
