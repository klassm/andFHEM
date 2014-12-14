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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.GenericDeviceAdapter;
import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesSwitchActionRow;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.adapter.devices.genericui.HolderActionRow;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.LightSceneDevice;
import li.klass.fhem.service.intent.DeviceIntentService;

import static android.widget.LinearLayout.VERTICAL;

public class LightSceneAdapter extends GenericDeviceAdapter<LightSceneDevice> {
    public LightSceneAdapter() {
        super(LightSceneDevice.class);
    }

    @Override
    protected void fillDeviceOverviewView(final View view, LightSceneDevice device, long lastUpdate) {
        TableLayout layout = (TableLayout) view.findViewById(R.id.device_overview_generic);
        layout.removeAllViews();

        new HolderActionRow<LightSceneDevice, String>(device.getAliasOrName(),
                HolderActionRow.LAYOUT_OVERVIEW) {

            @Override
            public List<String> getItems(LightSceneDevice device) {
                return device.getScenes();
            }

            @Override
            public View viewFor(String scene, LightSceneDevice device, LayoutInflater inflater, Context context, ViewGroup viewGroup) {
                Button button = (Button) inflater.inflate(R.layout.button, null);
                setSceneButtonProperties(context, device, scene, button, scene);
                return button;
            }
        }.createRow(view.getContext(), getInflater(), layout, device);
    }

    private void setSceneButtonProperties(final Context context, final LightSceneDevice device, final String scene, Button button, String buttonText) {
        button.setText(buttonText);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activateScene(device, scene, context);
            }
        });
    }

    private void activateScene(LightSceneDevice device, String scene, Context context) {
        Intent intent = new Intent(Actions.DEVICE_SET_SUB_STATE);
        intent.setClass(context, DeviceIntentService.class);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.STATE_NAME, "scene");
        intent.putExtra(BundleExtraKeys.STATE_VALUE, scene);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context));
        context.startService(intent);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        detailActions.add(new DeviceDetailViewAction<LightSceneDevice>() {
            @Override
            public View createView(final Context context, LayoutInflater inflater,
                                   final LightSceneDevice device, LinearLayout parent) {
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(VERTICAL);

                for (final String scene : device.getScenes()) {

                    Button button = (Button) inflater.inflate(R.layout.button_device_detail, parent, false);
                    assert (button != null);

                    String buttonText = String.format(context.getString(R.string.activateScene), scene);
                    setSceneButtonProperties(context, device, scene, button, buttonText);

                    layout.addView(button);
                }

                return layout;
            }
        });
        detailActions.add(new AvailableTargetStatesSwitchActionRow<LightSceneDevice>());
    }
}
