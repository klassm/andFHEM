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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.common.collect.ImmutableSet;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.advertisement.AdvertisementService;
import li.klass.fhem.service.graph.gplot.SvgGraphDefinition;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.service.intent.FavoritesIntentService;
import li.klass.fhem.service.intent.RoomListIntentService;
import li.klass.fhem.service.room.FavoritesService;
import li.klass.fhem.util.FhemResultReceiver;
import li.klass.fhem.util.device.DeviceActionUtil;
import li.klass.fhem.widget.notification.NotificationSettingView;

import static li.klass.fhem.constants.Actions.DEVICE_GRAPH_DEFINITIONS;
import static li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG;
import static li.klass.fhem.constants.Actions.FAVORITE_ADD;
import static li.klass.fhem.constants.Actions.FAVORITE_REMOVE;
import static li.klass.fhem.constants.Actions.GET_DEVICE_FOR_NAME;
import static li.klass.fhem.constants.Actions.SHOW_EXECUTING_DIALOG;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_DISPLAY_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_GRAPH_DEFINITION;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;
import static li.klass.fhem.constants.BundleExtraKeys.LAST_UPDATE;
import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;
import static li.klass.fhem.constants.ResultCodes.SUCCESS;

public class DeviceDetailFragment extends BaseFragment {
    @Inject
    FavoritesService favoritesService;

    @Inject
    AdvertisementService advertisementService;
    private String deviceName;
    private FhemDevice device;
    private String connectionId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        setArgumentsFrom(args);
    }

    private void setArgumentsFrom(Bundle args) {
        if (args == null) {
            return;
        }
        deviceName = args.getString(DEVICE_NAME);
        connectionId = args.getString(CONNECTION_ID);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DEVICE_NAME, deviceName);
        outState.putString(CONNECTION_ID, connectionId);
    }

    @Override
    protected void inject(@NonNull ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setArgumentsFrom(savedInstanceState);
        View superView = super.onCreateView(inflater, container, savedInstanceState);
        if (superView != null) return superView;

        View view = inflater.inflate(R.layout.device_detail_view, container, false);
        advertisementService.addAd(view, getActivity());

        return view;
    }

    @Override
    public void update(boolean refresh) {
        hideEmptyView();

        if (refresh) getActivity().sendBroadcast(new Intent(SHOW_EXECUTING_DIALOG));

        getActivity().startService(new Intent(GET_DEVICE_FOR_NAME)
                .setClass(getActivity(), RoomListIntentService.class)
                .putExtra(CONNECTION_ID, connectionId)
                .putExtra(DO_REFRESH, refresh)
                .putExtra(DEVICE_NAME, deviceName)
                .putExtra(RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        super.onReceiveResult(resultCode, resultData);

                        FragmentActivity activity = getActivity();
                        if (activity == null) return;

                        activity.sendBroadcast(new Intent(DISMISS_EXECUTING_DIALOG));

                        if (resultCode == SUCCESS && getView() != null) {
                            device = (FhemDevice) resultData.getSerializable(DEVICE);
                            ImmutableSet<SvgGraphDefinition> grapDefinitions = (ImmutableSet<SvgGraphDefinition>) resultData.getSerializable(DEVICE_GRAPH_DEFINITIONS);
                            long lastUpdate = resultData.getLong(LAST_UPDATE);

                            if (device == null) return;

                            DeviceAdapter adapter = DeviceType.getAdapterFor(device);
                            if (adapter == null) {
                                return;
                            }
                            loadGraphs();
                            activity.supportInvalidateOptionsMenu();
                            ScrollView scrollView = findScrollView();
                            if (scrollView != null) {
                                scrollView.removeAllViews();
                                scrollView.addView(adapter.createDetailView(activity, device, grapDefinitions, connectionId, lastUpdate));
                            }
                        }
                    }
                }));
    }

    private void loadGraphs() {

        getActivity().startService(new Intent(DEVICE_GRAPH_DEFINITIONS)
                .setClass(getActivity(), DeviceIntentService.class)
                .putExtra(CONNECTION_ID, connectionId)
                .putExtra(DEVICE_NAME, deviceName)
                .putExtra(RESULT_RECEIVER, new FhemResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode != SUCCESS
                                || getView() == null
                                || resultData == null
                                || !resultData.containsKey(DEVICE_GRAPH_DEFINITION)) {
                            return;
                        }
                        View detailView = findScrollView().getChildAt(0);
                        DeviceAdapter adapter = DeviceType.getAdapterFor(device);
                        if (adapter == null) {
                            return;
                        }
                        ImmutableSet<SvgGraphDefinition> graphs = (ImmutableSet<SvgGraphDefinition>) resultData.get(DEVICE_GRAPH_DEFINITION);
                        adapter.attachGraphs(getActivity(), detailView, graphs, connectionId, device);
                        detailView.invalidate();
                    }
                }));
    }

    private ScrollView findScrollView() {
        return (ScrollView) getView().findViewById(R.id.deviceDetailView);
    }

    @Override
    public CharSequence getTitle(Context context) {
        String name = getArguments().getString(DEVICE_DISPLAY_NAME);
        if (name == null) {
            name = getArguments().getString(DEVICE_NAME);
        }
        return name;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (device != null) {
            inflater.inflate(R.menu.device_menu, menu);
            if (favoritesService.isFavorite(deviceName, getActivity())) {
                menu.removeItem(R.id.menu_favorites_add);
            } else {
                menu.removeItem(R.id.menu_favorites_remove);
            }
            menu.removeItem(R.id.menu_rename);
            menu.removeItem(R.id.menu_delete);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_favorites_add:
            case R.id.menu_favorites_remove: {
                final boolean isAdd = item.getItemId() == R.id.menu_favorites_add;
                getActivity().startService(new Intent(isAdd ? FAVORITE_ADD : FAVORITE_REMOVE)
                        .setClass(getActivity(), FavoritesIntentService.class)
                        .putExtra(DEVICE, device)
                        .putExtra(RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                if (resultCode != SUCCESS) return;

                                Toast.makeText(getActivity(),
                                        isAdd ? R.string.context_favoriteadded : R.string.context_favoriteremoved,
                                        Toast.LENGTH_SHORT).show();
                                update(false);
                            }
                        }));
                break;
            }
            case R.id.menu_room:
                DeviceActionUtil.moveDevice(getActivity(), device);
                break;
            case R.id.menu_alias:
                DeviceActionUtil.setAlias(getActivity(), device);
                break;
            case R.id.menu_notification:
                new NotificationSettingView(getActivity(), deviceName).show(getActivity());
                break;
            default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean canChildScrollUp() {
        return super.canChildScrollUp() || findScrollView().getScrollY() > 0;
    }
}
