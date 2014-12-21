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

import li.klass.fhem.R;
import li.klass.fhem.appwidget.view.widget.base.otherWidgets.OtherWidgetsFragment;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.fragments.core.DeviceDetailFragment;
import li.klass.fhem.fragments.device.DeviceNameListNavigationFragment;
import li.klass.fhem.fragments.device.DeviceNameSelectionFragment;
import li.klass.fhem.fragments.device.DeviceNameSelectionNavigationFragment;
import li.klass.fhem.fragments.weekprofile.FromToWeekProfileFragment;
import li.klass.fhem.fragments.weekprofile.IntervalWeekProfileFragment;

public enum FragmentType {
    FAVORITES(FavoritesFragment.class, R.string.favorites, null),
    ROOM_LIST(RoomListFragment.class, R.string.roomList, null),
    ALL_DEVICES(AllDevicesFragment.class, R.string.alldevices, RoomListFragment.class),
    CONVERSION(ConversionFragment.class, R.string.conversion, null),
    DEVICE_DETAIL(DeviceDetailFragment.class, DeviceNameListNavigationFragment.class),
    FROM_TO_WEEK_PROFILE(FromToWeekProfileFragment.class),
    INTERVAL_WEEK_PROFILE(IntervalWeekProfileFragment.class),
    FLOORPLAN(FloorplanFragment.class),
    PREMIUM(PremiumFragment.class),
    ROOM_DETAIL(RoomDetailFragment.class, RoomListFragment.class),
    SEND_COMMAND(SendCommandFragment.class, R.string.send_command, null),
    DEVICE_SELECTION(DeviceNameSelectionFragment.class, DeviceNameSelectionNavigationFragment.class),
    DEVICE_NAME_LIST_NAVIGATION(DeviceNameListNavigationFragment.class),
    TIMER_OVERVIEW(TimerListFragment.class, R.string.timer, null),
    TIMER_DETAIL(TimerDetailFragment.class),
    CONNECTION_LIST(ConnectionListFragment.class),
    CONNECTION_DETAIL(ConnectionDetailFragment.class, ConnectionListFragment.class),
    WEB_VIEW(WebViewFragment.class),
    OTHER_WIDGETS_FRAGMENT(OtherWidgetsFragment.class, R.string.widget_others, null);

    private Class<? extends BaseFragment> fragmentClass;
    private Class<? extends BaseFragment> navigationFragment;
    private int fragmentTitle;

    FragmentType(Class<? extends BaseFragment> fragmentClass) {
        this(fragmentClass, -1, null);
    }

    FragmentType(Class<? extends BaseFragment> fragmentClass, int fragmentTitle,
                 Class<? extends BaseFragment> navigationFragment) {
        this.fragmentClass = fragmentClass;
        this.fragmentTitle = fragmentTitle;
        this.navigationFragment = navigationFragment;
    }

    FragmentType(Class<? extends BaseFragment> fragmentClass, Class<? extends BaseFragment> navigationClass) {
        this(fragmentClass, -1, navigationClass);
    }

    @SuppressWarnings("unchecked")
    public static FragmentType getFragmentFor(String name) {
        try {
            Class<? extends BaseFragment> fragmentType = (Class<? extends BaseFragment>) Class.forName(name);
            return FragmentType.getFragmentFor(fragmentType);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static FragmentType getFragmentFor(Class<? extends BaseFragment> clazz) {
        for (FragmentType fragmentType : FragmentType.values()) {
            if (fragmentType.fragmentClass.isAssignableFrom(clazz)) {
                return fragmentType;
            }
        }
        return null;
    }

    public static FragmentType forEnumName(String name) {
        try {
            return FragmentType.valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

    public Class<? extends BaseFragment> getNavigationClass() {
        return navigationFragment;
    }

    public Class<? extends BaseFragment> getContentClass() {
        return fragmentClass;
    }

    public boolean isTopLevelFragment() {
        return fragmentTitle != -1;
    }

    public int getFragmentTitle() {
        return fragmentTitle;
    }
}
