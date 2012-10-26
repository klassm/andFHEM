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

package li.klass.fhem.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.*;
import android.widget.*;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.adapter.timer.TimerListAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.AtDevice;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.util.device.DeviceActionUtil;

import java.util.ArrayList;
import java.util.List;

public class TimerListFragment extends BaseFragment {

    private static final String TAG = TimerListFragment.class.getName();
    private transient TimerListAdapter listAdapter;

    private static final int CONTEXT_MENU_DELETE = 1;
    private AtDevice contextMenuClickedDevice;

    private boolean createNewDeviceCalled = false;

    @SuppressWarnings("unused")
    public TimerListFragment(Bundle bundle) {
        super(bundle);
    }

    @SuppressWarnings("unused")
    public TimerListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            return view;
        }
        Context context = getActivity();

        listAdapter = new TimerListAdapter(context, R.layout.timer_list_item, new ArrayList<AtDevice>());

        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.timer_overview, null);
        TextView emptyView = (TextView) layout.findViewById(android.R.id.empty);
        ListView listView = (ListView) layout.findViewById(R.id.list);

        listView.setEmptyView(emptyView);
        listView.setAdapter(listAdapter);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AtDevice device = (AtDevice) view.getTag();

                Intent intent = new Intent(Actions.SHOW_FRAGMENT);
                intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.TIMER_DETAIL);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                getActivity().sendBroadcast(intent);
            }
        });

        Button createNewButton = (Button) layout.findViewById(R.id.timer_create_new);
        createNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewDeviceCalled = true;

                Intent intent = new Intent(Actions.SHOW_FRAGMENT);
                intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.TIMER_DETAIL);
                getActivity().sendBroadcast(intent);
            }
        });

        return layout;
    }

    @Override
    public void update(boolean doUpdate) {
        Intent intent = new Intent(Actions.GET_ALL_ROOMS_DEVICE_LIST);
        intent.putExtra(BundleExtraKeys.DO_REFRESH, doUpdate);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != ResultCodes.SUCCESS || !resultData.containsKey(BundleExtraKeys.DEVICE_LIST)) {
                    return;
                }

                RoomDeviceList roomDeviceList = (RoomDeviceList) resultData.getSerializable(BundleExtraKeys.DEVICE_LIST);
                List<AtDevice> devices = roomDeviceList.getDevicesOfType(DeviceType.AT);
                for (AtDevice atDevice : new ArrayList<AtDevice>(devices)) {
                    if (!atDevice.isSupported()) devices.remove(atDevice);
                }
                listAdapter.updateData(devices);

                Intent intent = new Intent(Actions.DISMISS_UPDATING_DIALOG);
                AndFHEMApplication.getContext().sendBroadcast(intent);
            }
        });
        getActivity().startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (createNewDeviceCalled) {
            createNewDeviceCalled = false;
            update(true);
        } else {
            update(false);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        AtDevice atDevice = (AtDevice) info.targetView.getTag();

        Log.e(TAG, atDevice.getName() + " context menu");

        menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.context_delete);

        this.contextMenuClickedDevice = atDevice;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CONTEXT_MENU_DELETE:
                DeviceActionUtil.deleteDevice(getActivity(), contextMenuClickedDevice);
                return true;
        }

        return super.onContextItemSelected(item);

    }
}
