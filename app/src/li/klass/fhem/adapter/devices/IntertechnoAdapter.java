/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.view.View;
import android.widget.TableLayout;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.domain.IntertechnoDevice;

public class IntertechnoAdapter extends ToggleableAdapter<IntertechnoDevice> {

    public IntertechnoAdapter() {
        super(IntertechnoDevice.class);
    }

    @Override
    public void fillDeviceOverviewView(View view, final IntertechnoDevice device) {
        TableLayout layout = (TableLayout) view.findViewById(R.id.device_overview_generic);
        layout.findViewById(R.id.deviceName).setVisibility(View.GONE);
        addOverviewSwitchActionRow(view.getContext(), device, layout);
    }

    @Override
    protected void afterPropertiesSet() {
        fieldNameAddedListeners.put("state", new FieldNameAddedToDetailListener<IntertechnoDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, IntertechnoDevice device,
                                         android.widget.TableRow fieldTableRow) {
                addDetailSwitchActionRow(context, device, tableLayout);
            }
        });
    }
}
