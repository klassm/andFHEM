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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.core.GenericDeviceAdapterWithSwitchActionRow;
import li.klass.fhem.adapter.devices.genericui.HolderActionRow;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.LightSceneDevice;
import li.klass.fhem.domain.core.FhemDevice;

public class LightSceneAdapter extends GenericDeviceAdapterWithSwitchActionRow<LightSceneDevice> {
    @Inject
    StateUiService stateUiService;

    public LightSceneAdapter() {
        super(LightSceneDevice.class);
    }

    @Override
    public View createOverviewView(LayoutInflater layoutInflater, View convertView, FhemDevice rawDevice, long lastUpdate) {
        TableLayout layout = (TableLayout) layoutInflater.inflate(R.layout.device_overview_generic, null);
        layout.removeAllViews();

        LightSceneDevice device = (LightSceneDevice) rawDevice;
        layout.addView(new HolderActionRow<LightSceneDevice, String>(device.getAliasOrName(),
                HolderActionRow.LAYOUT_OVERVIEW) {

            @Override
            public List<String> getItems(LightSceneDevice device) {
                return device.getScenes();
            }

            @Override
            public View viewFor(String scene, LightSceneDevice device, LayoutInflater inflater, Context context, ViewGroup viewGroup) {
                Button button = (Button) inflater.inflate(R.layout.lightscene_button, viewGroup, false);
                setSceneButtonProperties(device, scene, button);
                return button;
            }
        }.createRow(layout.getContext(), getInflater(), layout, device));
        return layout;
    }


    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();
        registerFieldListener("state", new FieldNameAddedToDetailListener<LightSceneDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, LightSceneDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new HolderActionRow<LightSceneDevice, String>(context.getString(R.string.scene), HolderActionRow.LAYOUT_DETAIL) {

                    @Override
                    public List<String> getItems(LightSceneDevice device) {
                        return device.getScenes();
                    }

                    @Override
                    public View viewFor(String scene, LightSceneDevice device, LayoutInflater inflater, Context context, ViewGroup viewGroup) {
                        Button button = (Button) inflater.inflate(R.layout.lightscene_button, viewGroup, false);
                        setSceneButtonProperties(device, scene, button);
                        return button;
                    }
                }.createRow(context, getInflater(), tableLayout, device));
            }
        });
    }

    private void setSceneButtonProperties(final LightSceneDevice device, final String scene, Button button) {
        button.setText(scene);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activateScene(device, scene);
            }
        });
    }

    private void activateScene(LightSceneDevice device, String scene) {
        stateUiService.setSubState(device, "scene", scene, getContext());
    }
}
