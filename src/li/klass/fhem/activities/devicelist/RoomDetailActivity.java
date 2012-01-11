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

package li.klass.fhem.activities.devicelist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.RoomDeviceList;

import static li.klass.fhem.constants.BundleExtraKeys.*;

public class RoomDetailActivity extends DeviceListActivity {

    private String roomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        roomName = extras.getString(ROOM_NAME);

        String roomTitlePrefix = getResources().getString(R.string.roomTitlePrefix);
        setTitle(roomTitlePrefix + " " + roomName);
    }

    @Override
    public void update(boolean doUpdate) {
        Intent intent = new Intent(Actions.GET_ROOM_DEVICE_LIST);
        intent.putExtras(new Bundle());
        intent.putExtra(DO_REFRESH, doUpdate);
        intent.putExtra(ROOM_NAME, roomName);
        intent.putExtra(RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == ResultCodes.SUCCESS) {
                    super.onReceiveResult(resultCode, resultData);
                    RoomDeviceList deviceList = (RoomDeviceList) resultData.getSerializable(DEVICE_LIST);
                    adapter.updateData(deviceList);
                }
            }
        });
        startService(intent);
    }
}
