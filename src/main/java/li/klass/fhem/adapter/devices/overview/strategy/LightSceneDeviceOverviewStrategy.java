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

package li.klass.fhem.adapter.devices.overview.strategy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem;
import li.klass.fhem.adapter.devices.genericui.HolderActionRow;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.LightSceneDevice;
import li.klass.fhem.domain.core.FhemDevice;

@Singleton
public class LightSceneDeviceOverviewStrategy extends OverviewStrategy {
    @Inject
    StateUiService stateUiService;

    @Inject
    public LightSceneDeviceOverviewStrategy() {
    }

    @Override
    public View createOverviewView(LayoutInflater layoutInflater, View convertView, FhemDevice rawDevice, long lastUpdate, List<DeviceViewItem> deviceItems) {
        TableLayout layout = (TableLayout) layoutInflater.inflate(R.layout.device_overview_generic, null);
        layout.removeAllViews();

        LightSceneDevice device = (LightSceneDevice) rawDevice;
        layout.addView(new HolderActionRow<String>(device.getAliasOrName(),
                HolderActionRow.LAYOUT_OVERVIEW) {

            @Override
            public List<String> getItems(FhemDevice device) {
                return ((LightSceneDevice) device).getScenes();
            }

            @Override
            public View viewFor(String scene, FhemDevice device, LayoutInflater inflater, Context context, ViewGroup viewGroup) {
                Button button = (Button) inflater.inflate(R.layout.lightscene_button, viewGroup, false);
                setSceneButtonProperties(device, scene, button, context);
                return button;
            }
        }.createRow(layout.getContext(), layoutInflater, layout, device));
        return layout;
    }

    @Override
    public boolean supports(FhemDevice fhemDevice) {
        return fhemDevice instanceof LightSceneDevice;
    }

    private void setSceneButtonProperties(final FhemDevice device, final String scene, Button button, final Context context) {
        button.setText(scene);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activateScene(device, scene, context);
            }
        });
    }

    private void activateScene(FhemDevice device, String scene, Context context) {
        stateUiService.setSubState(device, "scene", scene, context);
    }
}
