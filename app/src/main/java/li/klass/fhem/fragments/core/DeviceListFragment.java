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

package li.klass.fhem.fragments.core;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicReference;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.adapter.rooms.DeviceGridAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.fhem.DataConnectionSwitch;
import li.klass.fhem.fhem.DummyDataConnection;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.advertisement.AdvertisementUtil;
import li.klass.fhem.util.device.DeviceActionUtil;
import li.klass.fhem.widget.GridViewWithSections;
import li.klass.fhem.widget.notification.NotificationSettingView;

import static li.klass.fhem.constants.Actions.FAVORITE_ADD;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;
import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;
import static li.klass.fhem.constants.PreferenceKeys.DEVICE_LIST_RIGHT_PADDING;
import static li.klass.fhem.widget.GridViewWithSections.GridViewWithSectionsOnClickObserver;

public abstract class DeviceListFragment extends BaseFragment {

    public DeviceListFragment() {
    }

    public DeviceListFragment(Bundle bundle) {
        super(bundle);
    }

    /**
     * Attribute is set whenever a context menu concerning a device is clicked. This is the only way to actually get
     * the concerned device.
     */
    protected static AtomicReference<Device> contextMenuClickedDevice = new AtomicReference<Device>();
    protected static AtomicReference<DeviceListFragment> currentClickFragment = new AtomicReference<DeviceListFragment>();

    public static final int CONTEXT_MENU_FAVORITES_ADD = 1;
    public static final int CONTEXT_MENU_FAVORITES_DELETE = 2;
    public static final int CONTEXT_MENU_RENAME = 3;
    public static final int CONTEXT_MENU_DELETE = 4;
    public static final int CONTEXT_MENU_MOVE = 5;
    public static final int CONTEXT_MENU_ALIAS = 6;
    public static final int CONTEXT_MENU_NOTIFICATION = 7;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View superView = super.onCreateView(inflater, container, savedInstanceState);
        if (superView != null) return superView;


        View view = inflater.inflate(R.layout.room_detail, container, false);

        AdvertisementUtil.addAd(view, getActivity());

        assert view != null;

        GridViewWithSections nestedListView = (GridViewWithSections) view.findViewById(R.id.deviceMap1);
        assert nestedListView != null;

        LinearLayout emptyView = (LinearLayout) view.findViewById(R.id.emptyView);
        fillEmptyView(emptyView);

        if (! isNavigation()) {
            int rightPadding = ApplicationProperties.INSTANCE.getIntegerSharedPreference(DEVICE_LIST_RIGHT_PADDING, 0);
            nestedListView.setPadding(nestedListView.getPaddingLeft(), nestedListView.getPaddingTop(),
                    rightPadding, nestedListView.getPaddingBottom());
        }

        DeviceGridAdapter adapter = new DeviceGridAdapter(getActivity(), new RoomDeviceList(""));
        nestedListView.setAdapter(adapter);

        registerForContextMenu(nestedListView);

        adapter.registerOnClickObserver(new GridViewWithSectionsOnClickObserver() {
            @Override
            public void onItemClick(View view, Object parent, Object child, int parentPosition, int childPosition) {
                if (child == null || !(child instanceof Device)) {
                    return;
                }
                Device<?> device = (Device<?>) child;
                DeviceAdapter<? extends Device<?>> adapter = DeviceType.getAdapterFor(device);
                if (adapter != null && adapter.supportsDetailView(device)) {
                    adapter.gotoDetailView(getActivity(), device);
                }
            }
        });

        return view;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(boolean doUpdate) {

        View view = getView();
        if (view == null) return;

        showUpdatingBar();

        if (doUpdate) {
            view.invalidate();
        }

        Log.i(DeviceListFragment.class.getName(), "request device list update (doUpdate=" + doUpdate + ")");

        Intent intent = new Intent(getUpdateAction());
        intent.putExtras(new Bundle());
        intent.putExtra(DO_REFRESH, doUpdate);
        intent.putExtra(RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                View view = getView();
                if (view == null) return;

                if (resultCode == ResultCodes.SUCCESS && resultData.containsKey(DEVICE_LIST)) {
                    hideUpdatingBar();

                    RoomDeviceList deviceList = (RoomDeviceList) resultData.getSerializable(DEVICE_LIST);
                    getAdapter().updateData(deviceList);

                    if (deviceList != null && deviceList.isEmptyOrOnlyContainsDoNotShowDevices()) {
                        showEmptyView();
                    } else {
                        hideEmptyView();
                    }
                }

                View dummyConnectionNotification = view.findViewById(R.id.dummyConnectionNotification);
                if (!DataConnectionSwitch.INSTANCE.getCurrentProvider().getClass()
                        .isAssignableFrom(DummyDataConnection.class)) {
                    dummyConnectionNotification.setVisibility(View.GONE);
                } else {
                    dummyConnectionNotification.setVisibility(View.VISIBLE);
                }
            }
        });
        fillIntent(intent);

        FragmentActivity activity = getActivity();
        if (activity == null) {
            AndFHEMApplication.getContext().sendBroadcast(new Intent(Actions.RELOAD));
        } else {
            activity.startService(intent);
        }
    }

    protected void fillIntent(Intent intent) {
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Object tag = info.targetView.getTag();

        if (tag == null) return;
        if (tag instanceof Device) {
            contextMenuClickedDevice.set((Device) tag);
            currentClickFragment.set(this);

            menu.add(0, CONTEXT_MENU_FAVORITES_ADD, 0, R.string.context_addtofavorites);
            menu.add(0, CONTEXT_MENU_RENAME, 0, R.string.context_rename);
            menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.context_delete);
            menu.add(0, CONTEXT_MENU_MOVE, 0, R.string.context_move);
            menu.add(0, CONTEXT_MENU_ALIAS, 0, R.string.context_alias);
            menu.add(0, CONTEXT_MENU_NOTIFICATION, 0, R.string.context_notification);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);

        if (this != currentClickFragment.get()) return false;

        switch (item.getItemId()) {
            case CONTEXT_MENU_FAVORITES_ADD:
                Intent favoriteAddIntent = new Intent(FAVORITE_ADD);
                favoriteAddIntent.putExtra(BundleExtraKeys.DEVICE, contextMenuClickedDevice.get());
                favoriteAddIntent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        Toast.makeText(getActivity(), R.string.context_favoriteadded, Toast.LENGTH_SHORT).show();
                    }
                });
                getActivity().startService(favoriteAddIntent);

                return true;
            case CONTEXT_MENU_RENAME:
                DeviceActionUtil.renameDevice(getActivity(), contextMenuClickedDevice.get());
                return true;
            case CONTEXT_MENU_DELETE:
                DeviceActionUtil.deleteDevice(getActivity(), contextMenuClickedDevice.get());
                return true;
            case CONTEXT_MENU_MOVE:
                DeviceActionUtil.moveDevice(getActivity(), contextMenuClickedDevice.get());
                return true;
            case CONTEXT_MENU_ALIAS:
                DeviceActionUtil.setAlias(getActivity(), contextMenuClickedDevice.get());
                return true;
            case CONTEXT_MENU_NOTIFICATION:
                handleNotifications(contextMenuClickedDevice.get().getName());
                return true;
        }
        return false;
    }

    private void handleNotifications(String deviceName) {
        new NotificationSettingView(getActivity(), deviceName).show();
    }

    protected DeviceGridAdapter getAdapter() {
        GridViewWithSections listView = (GridViewWithSections) getView().findViewById(R.id.deviceMap1);
        return (DeviceGridAdapter) listView.getAdapter();
    }

    @Override
    protected void fillEmptyView(LinearLayout view) {
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view, null);
        assert emptyView != null;
        TextView emptyText = (TextView) emptyView.findViewById(R.id.emptyText);
        emptyText.setText(R.string.noDevices);

        view.addView(emptyView);
    }

    protected abstract String getUpdateAction();
}
