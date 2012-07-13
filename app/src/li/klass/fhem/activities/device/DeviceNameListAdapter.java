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

package li.klass.fhem.activities.device;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.adapter.rooms.DeviceGridAdapter;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceType;

public class DeviceNameListAdapter extends DeviceGridAdapter {
    private String selectedDeviceName;

    public DeviceNameListAdapter(Context context, RoomDeviceList roomDeviceList) {
        super(context, roomDeviceList);
    }

    @Override
    protected View getChildView(DeviceType parent, int parentPosition, Device<?> child, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.device_name_selection, null);

        TextView content = (TextView) view.findViewById(R.id.name);
        content.setText(child.getAliasOrName());

        view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (child.getName().equals(selectedDeviceName)) {
            view.setBackgroundColor(context.getResources().getColor(R.color.focusedColor));
        }

        return view;
    }

    public void updateData(RoomDeviceList roomDeviceList, String selectedDeviceName) {
        this.selectedDeviceName = selectedDeviceName;
        super.updateData(roomDeviceList);
    }
}

