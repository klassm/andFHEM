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

package li.klass.fhem.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import li.klass.fhem.R;
import li.klass.fhem.activities.base.BaseActivity;
import li.klass.fhem.activities.devicelist.RoomDetailActivity;
import li.klass.fhem.adapter.rooms.RoomListAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;

import java.util.ArrayList;
import java.util.List;

public class RoomListActivity extends BaseActivity<RoomListAdapter> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        update(false);
    }

    @Override
    protected RoomListAdapter initializeLayoutAndReturnAdapter() {
        ListView roomList = (ListView) findViewById(R.id.roomList);

        RoomListAdapter adapter = new RoomListAdapter(this, R.layout.room_list_name, new ArrayList<String>());
        roomList.setAdapter(adapter);

        roomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String roomName = String.valueOf(view.getTag());
                Intent intent = new Intent();
                intent.setClass(RoomListActivity.this, RoomDetailActivity.class);
                intent.putExtras(new Bundle());
                intent.putExtra(BundleExtraKeys.ROOM_NAME, roomName);

                startActivity(intent);
            }
        });

        return adapter;
    }

    @Override
    protected void setLayout() {
        setContentView(R.layout.room_list);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(boolean doUpdate) {
        adapter.updateData(new ArrayList<String>());

        Intent intent = new Intent(Actions.GET_ROOM_NAME_LIST);
        intent.putExtras(new Bundle());
        intent.putExtra(BundleExtraKeys.DO_REFRESH, doUpdate);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                List<String> roomList = (ArrayList<String>) resultData.getSerializable(BundleExtraKeys.ROOM_LIST);
                adapter.updateData(roomList);
            }
        });
        startService(intent);
    }
}
