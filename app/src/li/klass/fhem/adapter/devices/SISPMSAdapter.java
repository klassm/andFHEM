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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.domain.SISPMSDevice;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.util.device.FloorplanUtil;

public class SISPMSAdapter extends ToggleableAdapter<SISPMSDevice> {

    public SISPMSAdapter() {
        super(SISPMSDevice.class);
    }

    @Override
    public void fillDeviceOverviewView(View view, final SISPMSDevice device) {
        TableLayout layout = (TableLayout) view.findViewById(R.id.device_overview_generic);
        layout.findViewById(R.id.deviceName).setVisibility(View.GONE);

        addOverviewSwitchActionRow(view.getContext(), device, layout);
    }

    @Override
    protected void afterPropertiesSet() {
        fieldNameAddedListeners.put("state", new FieldNameAddedToDetailListener<SISPMSDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, SISPMSDevice device, android.widget.TableRow fieldTableRow) {
                addDetailSwitchActionRow(context, device, tableLayout);
            }
        });
    }

    @Override
    protected void fillFloorplanView(final Context context, final SISPMSDevice device, LinearLayout layout, FloorplanViewSettings viewSettings) {
        ImageView buttonView = FloorplanUtil.createSwitchStateBasedImageView(context, device);
        layout.addView(buttonView);
    }
}
