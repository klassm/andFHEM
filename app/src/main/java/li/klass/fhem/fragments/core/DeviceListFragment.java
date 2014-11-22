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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

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
import li.klass.fhem.service.advertisement.AdvertisementService;
import li.klass.fhem.service.intent.FavoritesIntentService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.FhemResultReceiver;
import li.klass.fhem.util.device.DeviceActionUtil;
import li.klass.fhem.widget.GridViewWithSections;
import li.klass.fhem.widget.notification.NotificationSettingView;

import static li.klass.fhem.constants.Actions.FAVORITE_ADD;
import static li.klass.fhem.constants.Actions.FAVORITE_REMOVE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;
import static li.klass.fhem.constants.BundleExtraKeys.IS_FAVORITE;
import static li.klass.fhem.constants.BundleExtraKeys.LAST_UPDATE;
import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;
import static li.klass.fhem.constants.PreferenceKeys.DEVICE_LIST_RIGHT_PADDING;

public abstract class DeviceListFragment extends BaseFragment {

    protected static AtomicReference<Device> contextMenuClickedDevice = new AtomicReference<>();
    protected static AtomicReference<DeviceListFragment> currentClickFragment = new AtomicReference<>();
    protected static AtomicBoolean isClickedDeviceFavorite = new AtomicBoolean(false);

    @Inject
    DataConnectionSwitch dataConnectionSwitch;

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    AdvertisementService advertisementService;

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.device_menu, menu);
            if (isClickedDeviceFavorite.get()) {
                menu.removeItem(R.id.menu_favorites_add);
            } else {
                menu.removeItem(R.id.menu_favorites_remove);
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_favorites_add:
                    Intent favoriteAddIntent = new Intent(FAVORITE_ADD);
                    favoriteAddIntent.setClass(getActivity(), FavoritesIntentService.class);
                    favoriteAddIntent.putExtra(BundleExtraKeys.DEVICE, contextMenuClickedDevice.get());
                    favoriteAddIntent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            if (resultCode != ResultCodes.SUCCESS) return;

                            Toast.makeText(getActivity(), R.string.context_favoriteadded, Toast.LENGTH_SHORT).show();
                        }
                    });
                    getActivity().startService(favoriteAddIntent);
                    break;
                case R.id.menu_favorites_remove:
                    Intent favoriteRemoveIntent = new Intent(FAVORITE_REMOVE);
                    favoriteRemoveIntent.setClass(getActivity(), FavoritesIntentService.class);
                    favoriteRemoveIntent.putExtra(BundleExtraKeys.DEVICE, contextMenuClickedDevice.get());
                    favoriteRemoveIntent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            if (resultCode != ResultCodes.SUCCESS) return;

                            Toast.makeText(getActivity(), R.string.context_favoriteremoved, Toast.LENGTH_SHORT).show();
                        }
                    });
                    getActivity().startService(favoriteRemoveIntent);
                    break;
                case R.id.menu_rename:
                    DeviceActionUtil.renameDevice(getActivity(), contextMenuClickedDevice.get());
                    break;
                case R.id.menu_delete:
                    DeviceActionUtil.deleteDevice(getActivity(), contextMenuClickedDevice.get());
                    break;
                case R.id.menu_room:
                    DeviceActionUtil.moveDevice(getActivity(), contextMenuClickedDevice.get());
                    break;
                case R.id.menu_alias:
                    DeviceActionUtil.setAlias(getActivity(), contextMenuClickedDevice.get());
                    break;
                case R.id.menu_notification:
                    handleNotifications(contextMenuClickedDevice.get().getName());
                    break;
                default:
                    return false;
            }
            actionMode.finish();
            update(false);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {

        }
    };
    private ActionMode actionMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View superView = super.onCreateView(inflater, container, savedInstanceState);
        if (superView != null) return superView;


        View view = inflater.inflate(R.layout.room_detail, container, false);

        advertisementService.addAd(view, getActivity());

        assert view != null;

        GridViewWithSections nestedListView = (GridViewWithSections) view.findViewById(R.id.deviceMap1);
        assert nestedListView != null;

        LinearLayout emptyView = (LinearLayout) view.findViewById(R.id.emptyView);
        fillEmptyView(emptyView, container);

        if (!isNavigation()) {
            int rightPadding = applicationProperties.getIntegerSharedPreference(DEVICE_LIST_RIGHT_PADDING, 0);
            nestedListView.setPadding(nestedListView.getPaddingLeft(), nestedListView.getPaddingTop(),
                    rightPadding, nestedListView.getPaddingBottom());
        }

        DeviceGridAdapter adapter = new DeviceGridAdapter(getActivity(), new RoomDeviceList(""), applicationProperties);
        nestedListView.setAdapter(adapter);
        nestedListView.setOnLongClickListener(new GridViewWithSections.OnClickListener<String, Device>() {
            @Override
            public boolean onItemClick(View view, String parent, final Device child, int parentPosition, int childPosition) {
                if (child == null) {
                    return false;
                } else {
                    Intent intent = new Intent(Actions.FAVORITES_IS_FAVORITES);
                    intent.setClass(getActivity(), FavoritesIntentService.class);
                    intent.putExtra(BundleExtraKeys.DEVICE_NAME, child.getName());
                    intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new FhemResultReceiver() {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            contextMenuClickedDevice.set(child);
                            isClickedDeviceFavorite.set(resultData.getBoolean(IS_FAVORITE));
                            actionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(actionModeCallback);
                        }
                    });
                    DeviceListFragment.this.getActivity().startService(intent);

                    return true;
                }
            }
        });

        nestedListView.setOnClickListener(new GridViewWithSections.OnClickListener<String, Device>() {
            @Override
            public boolean onItemClick(View view, String parent, Device child, int parentPosition, int childPosition) {
                if (child != null) {
                    DeviceAdapter<? extends Device> adapter = DeviceType.getAdapterFor(child);
                    if (adapter != null && adapter.supportsDetailView(child)) {
                        if (actionMode != null) actionMode.finish();
                        adapter.attach(DeviceListFragment.this.getActivity());
                        adapter.gotoDetailView(getActivity(), child);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });


        return view;
    }

    protected void fillEmptyView(LinearLayout view, ViewGroup viewGroup) {
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view, viewGroup, false);
        assert emptyView != null;
        TextView emptyText = (TextView) emptyView.findViewById(R.id.emptyText);
        emptyText.setText(R.string.noDevices);

        view.addView(emptyView);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(boolean doUpdate) {

        View view = getView();
        if (view == null) return;


        if (doUpdate) {
            getActivity().sendBroadcast(new Intent(Actions.SHOW_EXECUTING_DIALOG));
            view.invalidate();
        }

        Log.i(DeviceListFragment.class.getName(), "request device list update (doUpdate=" + doUpdate + ")");

        Intent intent = new Intent(getUpdateAction());
        intent.setClass(getActivity(), getUpdateActionIntentTargetClass());
        intent.putExtras(new Bundle());
        intent.putExtra(DO_REFRESH, doUpdate);
        intent.putExtra(RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                View view = getView();
                if (view == null) return;

                if (resultCode == ResultCodes.SUCCESS && resultData.containsKey(DEVICE_LIST)) {
                    getActivity().sendBroadcast(new Intent(Actions.DISMISS_EXECUTING_DIALOG));

                    RoomDeviceList deviceList = (RoomDeviceList) resultData.getSerializable(DEVICE_LIST);
                    long lastUpdate = resultData.getLong(LAST_UPDATE);

                    getAdapter().updateData(deviceList, lastUpdate);

                    if (deviceList != null && deviceList.isEmptyOrOnlyContainsDoNotShowDevices()) {
                        showEmptyView();
                    } else {
                        hideEmptyView();
                    }
                }

                View dummyConnectionNotification = view.findViewById(R.id.dummyConnectionNotification);
                if (!dataConnectionSwitch.getCurrentProvider().getClass()
                        .isAssignableFrom(DummyDataConnection.class)) {
                    dummyConnectionNotification.setVisibility(View.GONE);
                } else {
                    dummyConnectionNotification.setVisibility(View.VISIBLE);
                }
            }
        });
        fillIntent(intent);

        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.startService(intent);
        }
    }

    protected abstract Class<?> getUpdateActionIntentTargetClass();

    protected abstract String getUpdateAction();

    protected DeviceGridAdapter getAdapter() {
        GridViewWithSections listView = getDeviceList();
        return (DeviceGridAdapter) listView.getAdapter();
    }

    protected void fillIntent(Intent intent) {
    }

    private GridViewWithSections getDeviceList() {
        if (getView() == null) {
            return null;
        } else {
            return (GridViewWithSections) getView().findViewById(R.id.deviceMap1);
        }
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

            ((ActionBarActivity) getActivity()).startSupportActionMode(actionModeCallback);
        }
    }

    private void handleNotifications(String deviceName) {
        new NotificationSettingView(getActivity(), deviceName).show(getActivity());
    }

    @Override
    public void invalidate() {
        super.invalidate();

        getAdapter().restoreParents();

        getDeviceList().updateNumberOfColumns();
        getAdapter().notifyDataSetInvalidated();
    }
}
