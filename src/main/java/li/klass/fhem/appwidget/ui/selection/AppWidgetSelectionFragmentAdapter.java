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

package li.klass.fhem.appwidget.ui.selection;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.ui.widget.WidgetSize;
import li.klass.fhem.appwidget.ui.widget.WidgetType;
import li.klass.fhem.appwidget.ui.widget.base.otherWidgets.OtherWidgetsFragment;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.fragments.device.DeviceNameSelectionFragment;
import li.klass.fhem.room.list.ui.RoomListFragment;
import li.klass.fhem.util.FhemResultReceiver;

import static li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_SIZE;
import static li.klass.fhem.constants.BundleExtraKeys.EMPTY_TEXT_ID;
import static li.klass.fhem.constants.BundleExtraKeys.ON_CLICKED_CALLBACK;
import static li.klass.fhem.constants.BundleExtraKeys.ROOM_SELECTABLE_CALLBACK;

class AppWidgetSelectionFragmentAdapter extends FragmentPagerAdapter {

    private final WidgetSize widgetSize;
    private final Context context;
    private final SelectionCompletedCallback selectionCompletedCallback;

    AppWidgetSelectionFragmentAdapter(FragmentManager fm, Context context, WidgetSize widgetSize, SelectionCompletedCallback selectionCompletedCallback) {
        super(fm);
        this.widgetSize = widgetSize;
        this.context = context;
        this.selectionCompletedCallback = selectionCompletedCallback;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return devicesFragment();
            case 1:
                return roomsFragment();
            case 2:
                return othersFragment();
        }
        throw new IllegalStateException("cannot handle position " + position);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.widget_devices);
            case 1:
                return context.getString(R.string.widget_rooms);
            case 2:
                return context.getString(R.string.widget_others);
        }
        throw new IllegalStateException("cannot handle position " + position);
    }


    private DeviceNameSelectionFragment devicesFragment() {
        Bundle bundle = new Bundle();

        bundle.putSerializable(BundleExtraKeys.DEVICE_FILTER, new DeviceNameSelectionFragment.DeviceFilter() {
            @Override
            public boolean isSelectable(FhemDevice device) {
                return !WidgetType.Companion.getSupportedDeviceWidgetsFor(widgetSize, device, context).isEmpty();
            }
        });

        bundle.putParcelable(BundleExtraKeys.RESULT_RECEIVER, new FhemResultReceiver() {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != ResultCodes.SUCCESS ||
                        !resultData.containsKey(BundleExtraKeys.CLICKED_DEVICE)) return;

                FhemDevice clickedDevice = (FhemDevice) resultData.getSerializable(BundleExtraKeys.CLICKED_DEVICE);
                selectionCompletedCallback.onDeviceSelect(clickedDevice);
            }
        });

        bundle.putInt(EMPTY_TEXT_ID, R.string.widgetNoDevices);

        DeviceNameSelectionFragment deviceSelectionFragment = new DeviceNameSelectionFragment();
        deviceSelectionFragment.setArguments(bundle);

        return deviceSelectionFragment;
    }

    private RoomListFragment roomsFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ROOM_SELECTABLE_CALLBACK, new RoomListFragment.RoomSelectableCallback() {
            @Override
            public boolean isRoomSelectable(String roomName) {
                return !WidgetType.Companion.getSupportedRoomWidgetsFor(widgetSize).isEmpty();
            }
        });
        bundle.putSerializable(ON_CLICKED_CALLBACK, new RoomListFragment.RoomClickedCallback() {
            @Override
            public void onRoomClicked(String roomName) {
                selectionCompletedCallback.onRoomSelect(roomName);
            }
        });
        bundle.putInt(EMPTY_TEXT_ID, R.string.widgetNoRooms);

        RoomListFragment fragment = new RoomListFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private OtherWidgetsFragment othersFragment() {
        Bundle arguments = new Bundle();
        arguments.putSerializable(APP_WIDGET_SIZE, widgetSize);
        arguments.putSerializable(ON_CLICKED_CALLBACK, new OtherWidgetsFragment.OnWidgetClickedCallback() {
            @Override
            public void onWidgetClicked(WidgetType widgetType) {
                selectionCompletedCallback.onOtherWidgetSelect(widgetType);
            }
        });

        OtherWidgetsFragment otherWidgetsFragment = new OtherWidgetsFragment();
        otherWidgetsFragment.setArguments(arguments);

        return otherWidgetsFragment;
    }
}
