/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 *  server.
 *
 *  Copyright (c) 2012, Matthias Klass or third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by Red Hat Inc.
 *
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 *  for more details.
 *
 *  You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 *  along with this distribution; if not, write to:
 *    Free Software Foundation, Inc.
 *    51 Franklin Street, Fifth Floor
 */

package li.klass.fhem.adapter.devices;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.*;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesSwitchActionRow;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.TRXLightDevice;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.util.device.FloorplanUtil;

public class TRXLightAdapter extends ToggleableAdapter<TRXLightDevice> {

    public TRXLightAdapter() {
        super(TRXLightDevice.class);
    }

    @Override
    public void fillDeviceOverviewView(View view, final TRXLightDevice device) {
        TableLayout layout = (TableLayout) view.findViewById(R.id.device_overview_generic);
        layout.findViewById(R.id.deviceName).setVisibility(View.GONE);

        addOverviewSwitchActionRow(view.getContext(), device, layout);
    }

    @Override
    protected void afterPropertiesSet() {
        fieldNameAddedListeners.put("state", new FieldNameAddedToDetailListener<TRXLightDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, TRXLightDevice device, TableRow fieldTableRow) {
                    addDetailSwitchActionRow(context, device, tableLayout);
            }
        });

        detailActions.add(new AvailableTargetStatesSwitchActionRow<TRXLightDevice>());
    }

    @Override
    protected void fillFloorplanView(final Context context, final TRXLightDevice device, LinearLayout layout, FloorplanViewSettings viewSettings) {
        ImageView buttonView = FloorplanUtil.createSwitchStateBasedImageView(context, device);
        layout.addView(buttonView);
    }
}
