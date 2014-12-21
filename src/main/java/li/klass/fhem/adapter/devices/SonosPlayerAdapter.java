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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.core.GenericDeviceAdapter;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.adapter.devices.genericui.SeekBarActionRowFullWidthAndButton;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.SonosPlayerDevice;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.util.ApplicationProperties;

public class SonosPlayerAdapter extends GenericDeviceAdapter<SonosPlayerDevice> {

    @Inject
    ApplicationProperties applicationProperties;

    public SonosPlayerAdapter() {
        super(SonosPlayerDevice.class);
    }

    @Override
    protected void afterPropertiesSet() {
        registerFieldListener("volume", new FieldNameAddedToDetailListener<SonosPlayerDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, SonosPlayerDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new SeekBarActionRowFullWidthAndButton<SonosPlayerDevice>(context, Integer.valueOf(device.getVolume()), 100) {

                    @Override
                    public void onButtonSetValue(SonosPlayerDevice device, int value) {
                        onStopTrackingTouch(context, device, value);
                    }

                    @Override
                    protected ApplicationProperties getApplicationProperties() {
                        return applicationProperties;
                    }

                    @Override
                    public void onStopTrackingTouch(Context context, SonosPlayerDevice device, int progress) {
                        Intent intent = new Intent(Actions.DEVICE_SET_SUB_STATE);
                        intent.setClass(context, DeviceIntentService.class);
                        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                        intent.putExtra(BundleExtraKeys.STATE_NAME, "volume");
                        intent.putExtra(BundleExtraKeys.STATE_VALUE, progress + "");
                        putUpdateExtra(intent);

                        context.startService(intent);
                    }
                }.createRow(getInflater(), device));
            }
        });

        detailActions.add(new DeviceDetailViewAction<SonosPlayerDevice>() {
            @Override
            public View createView(Context context, LayoutInflater inflater, SonosPlayerDevice device, LinearLayout parent) {
                View view = inflater.inflate(R.layout.sonos_player_action, parent, false);

                fillImageButtonWithAction(context, view, device, R.id.rewind, "Previous");
                fillImageButtonWithAction(context, view, device, R.id.pause, "Pause");
                fillImageButtonWithAction(context, view, device, R.id.stop, "Stop");
                fillImageButtonWithAction(context, view, device, R.id.play, "Play");
                fillImageButtonWithAction(context, view, device, R.id.forward, "Next");

                return view;
            }
        });
    }

    private void fillImageButtonWithAction(final Context context, View view, final SonosPlayerDevice device,
                                           int id, final String action) {
        ImageButton button = (ImageButton) view.findViewById(id);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendStateAction(context, device, action);
            }
        });
    }
}
