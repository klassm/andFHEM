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

package li.klass.fhem.adapter.rooms;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.adapter.ListDataAdapter;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.constants.PreferenceKeys.SHOW_HIDDEN_DEVICES;

public class RoomListAdapter extends ListDataAdapter<String> {
    private String selectedRoom;

    private static final Logger LOG = LoggerFactory.getLogger(RoomListAdapter.class);

    public RoomListAdapter(Context context, int resource, List<String> data) {
        super(context, resource, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String roomName = (String) getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(resource, null);
        }

        assert convertView != null;

        TextView roomNameTextView = (TextView) convertView.findViewById(R.id.roomName);
        roomNameTextView.setText(roomName);

        convertView.setTag(roomName);

        int backgroundColor = roomName.equals(selectedRoom) ? R.color.android_green : android.R.color.transparent;
        convertView.setBackgroundColor(context.getResources().getColor(backgroundColor));

        return convertView;
    }

    public void updateData(List<String> newData, String selectedRoom) {
        if (newData == null) return;

        setSelectedRoom(selectedRoom);

        SharedPreferences preferences = getDefaultSharedPreferences(context);
        boolean showHiddenDevices = preferences.getBoolean(SHOW_HIDDEN_DEVICES, false);
        if (! showHiddenDevices) {
            for (String roomName : newArrayList(newData)) {
                if (roomName.equalsIgnoreCase("hidden")) {
                    newData.remove(roomName);
                }
            }
        }

        updateData(newData);
    }

    private void setSelectedRoom(String selectedRoom) {
        LOG.info("set selected room to {}", selectedRoom);
        this.selectedRoom = selectedRoom;
    }

    @Override
    protected boolean doSort() {
        return false;
    }
}
