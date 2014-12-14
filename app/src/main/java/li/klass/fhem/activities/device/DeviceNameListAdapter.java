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
import android.widget.AbsListView;
import android.widget.TextView;

import li.klass.fhem.R;
import li.klass.fhem.adapter.rooms.DeviceGridAdapter;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.util.ApplicationProperties;

import static android.view.ViewGroup.LayoutParams;

public class DeviceNameListAdapter<DEVICE extends Device<DEVICE>> extends DeviceGridAdapter<DEVICE> {
    public static final int DEFAULT_REQUIRED_COLUMN_WIDTH = 250;
    private int requiredColumnWidth = DEFAULT_REQUIRED_COLUMN_WIDTH;
    private String selectedDeviceName;

    public DeviceNameListAdapter(Context context, RoomDeviceList roomDeviceList, int requiredColumnWidth,
                                 ApplicationProperties applicationProperties) {
        super(context, roomDeviceList, applicationProperties);
        this.requiredColumnWidth = requiredColumnWidth;
    }

    @Override
    protected View getChildView(String parent, int parentPosition, DEVICE child, View view, ViewGroup viewGroup) {
        if (child == null) {
            TextView fillerView = new TextView(context);
            fillerView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            return fillerView;
        }

        view = layoutInflater.inflate(R.layout.device_name_selection, viewGroup, false);

        TextView content = (TextView) view.findViewById(R.id.name);
        content.setText(child.getAliasOrName());

        view.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        if (child.getName().equals(selectedDeviceName)) {
            view.setBackgroundColor(context.getResources().getColor(R.color.android_green));
        }

        return view;
    }

    public void updateData(RoomDeviceList roomDeviceList, String selectedDeviceName, long lastUpdate) {
        this.selectedDeviceName = selectedDeviceName;
        super.updateData(roomDeviceList, lastUpdate);
    }

    public int getSelectedDevicePosition() {
        DEVICE device = roomDeviceList.getDeviceFor(selectedDeviceName);
        if (device == null) return 0;

        return getFlatPositionForParentAndChild(device.getInternalDeviceGroupOrGroupAttributes().get(0), device);
    }

    @Override
    protected int getRequiredColumnWidth() {
        return requiredColumnWidth;
    }
}

