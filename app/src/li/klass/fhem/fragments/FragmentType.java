/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.fragments;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.fragments.core.DeviceDetailFragment;
import li.klass.fhem.fragments.device.DeviceNameListNavigationFragment;
import li.klass.fhem.fragments.device.DeviceNameSelectionFragment;
import li.klass.fhem.fragments.device.DeviceNameSelectionNavigationFragment;
import li.klass.fhem.fragments.weekprofile.FromToWeekProfileFragment;
import li.klass.fhem.fragments.weekprofile.IntervalWeekProfileFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public enum FragmentType {
    ALL_DEVICES(AllDevicesFragment.class, R.string.tab_alldevices, 2, RoomListFragment.class),
    CONVERSION(ConversionFragment.class),
    FAVORITES(FavoritesFragment.class, R.string.tab_favorites, 0, null),
    DEVICE_DETAIL(DeviceDetailFragment.class, DeviceNameListNavigationFragment.class),
    FROM_TO_WEEK_PROFILE(FromToWeekProfileFragment.class),
    INTERVAL_WEEK_PROFILE(IntervalWeekProfileFragment.class),
    FLOORPLAN(FloorplanFragment.class),
    PREMIUM(PremiumFragment.class),
    ROOM_DETAIL(RoomDetailFragment.class, RoomListFragment.class),
    ROOM_LIST(RoomListFragment.class, R.string.tab_roomList, 1, null),
    SEND_COMMAND(SendCommandFragment.class),
    DEVICE_SELECTION(DeviceNameSelectionFragment.class, DeviceNameSelectionNavigationFragment.class),
    DEVICE_NAME_LIST_NAVIGATION(DeviceNameListNavigationFragment.class),
    TIMER_OVERVIEW(TimerListFragment.class),
    TIMER_DETAIL(TimerDetailFragment.class);

    private Class<? extends BaseFragment> fragmentClass;
    private Class<? extends BaseFragment> navigationFragment;
    private String topLevelTabName;

    private static Comparator<FragmentType> topLevelFragmentNameComparator = new Comparator<FragmentType>() {
        @Override
        public int compare(FragmentType me, FragmentType other) {
            if (me.topLevelTabName == null) return -1;
            return ((Integer) me.topLevelPosition).compareTo(other.topLevelPosition);
        }
    };
    private int topLevelPosition;

    FragmentType(Class<? extends BaseFragment> fragmentClass) {
        this(fragmentClass, null, -1, null);
    }

    FragmentType(Class<? extends BaseFragment> fragmentClass, Class<? extends BaseFragment> navigationFragment) {
        this(fragmentClass, null, -1, navigationFragment);
    }

    FragmentType(Class<? extends BaseFragment> fragmentClass, int topLevelTabStringId, int topLevelPosition,
                 Class<? extends BaseFragment> navigationFragment) {
        this(fragmentClass, topLevelTabStringId == -1 ? null : AndFHEMApplication.getContext().getString(topLevelTabStringId),
                topLevelPosition, navigationFragment);
    }

    FragmentType(Class<? extends BaseFragment> fragmentClass, String topLevelTabName, int topLevelPosition,
                 Class<? extends BaseFragment> navigationFragment) {
        this.fragmentClass = fragmentClass;
        this.topLevelTabName = topLevelTabName;
        this.topLevelPosition = topLevelPosition;
        this.navigationFragment = navigationFragment;
    }

    public static List<FragmentType> getTopLevelFragments() {
        List<FragmentType> topLevelFragmentTypes = new ArrayList<FragmentType>();
        for (FragmentType fragmentType : FragmentType.values()) {
            if (fragmentType.topLevelTabName != null) {
                topLevelFragmentTypes.add(fragmentType);
            }
        }
        Collections.sort(topLevelFragmentTypes, topLevelFragmentNameComparator);

        return topLevelFragmentTypes;
    }

    public static FragmentType getFragmentFor(Class<? extends BaseFragment> clazz) {
        for (FragmentType fragmentType : FragmentType.values()) {
            if (fragmentType.fragmentClass.isAssignableFrom(clazz)) {
                return fragmentType;
            }
        }
        return null;
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

    public Class<? extends BaseFragment> getNavigationClass() {
        return navigationFragment;
    }

    public Class<? extends BaseFragment> getContentClass() {
        return fragmentClass;
    }

    public String getTopLevelTabName() {
        return topLevelTabName;
    }

    public int getTopLevelPosition() {
        return topLevelPosition;
    }

    public boolean isTopLevelFragment() {
        return topLevelTabName != null;
    }
}
