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

package li.klass.fhem.activities.device;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import li.klass.fhem.adapter.rooms.DeviceListAdapter;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.RoomDeviceList;

public class DeviceSelectionAdapter extends DeviceListAdapter {

    public DeviceSelectionAdapter(Context context, RoomDeviceList roomDeviceList) {
        super(context, roomDeviceList);

    }

    @Override
    protected View getChildView(DeviceType parent, int parentPosition, Device<?> child, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
        TextView content = (TextView) view.findViewById(android.R.id.text1);
        content.setText(child.getAliasOrName());

        return view;
    }
}
