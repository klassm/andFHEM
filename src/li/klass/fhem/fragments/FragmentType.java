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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public enum FragmentType {
    ALL_DEVICES(AllDevicesFragment.class, R.string.tab_alldevices, 2),
    CONVERSION(ConversionFragment.class),
    FAVORITES(FavoritesFragment.class, R.string.tab_favorites, 0),
    FHT_TIMETABLE_CONTROL(FHTTimetableControlListFragment.class),
    FLOORPLAN(FloorplanFragment.class),
    PREMIUM(PremiumFragment.class),
    ROOM_DETAIL(RoomDetailFragment.class),
    ROOM_LIST(RoomListFragment.class, R.string.tab_roomList, 1),
    SEND_COMMAND(SendCommandFragment.class),
    DEVICE_SELECTION(DeviceSelectionFragment.class);

    private Class<? extends BaseFragment> fragmentClass;
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
        this(fragmentClass, null, -1);
    }

    FragmentType(Class<? extends BaseFragment> fragmentClass, int topLevelTabName, int topLevelPosition) {
        this(fragmentClass, AndFHEMApplication.getContext().getString(topLevelTabName), topLevelPosition);
    }

    FragmentType(Class<? extends BaseFragment> fragmentClass, String topLevelTabName, int topLevelPosition) {
        this.fragmentClass = fragmentClass;
        this.topLevelTabName = topLevelTabName;
        this.topLevelPosition = topLevelPosition;
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

    public Class<? extends BaseFragment> getFragmentClass() {
        return fragmentClass;
    }

    public String getTopLevelTabName() {
        return topLevelTabName;
    }

    public int getTopLevelPosition() {
        return topLevelPosition;
    }

    public boolean isTopLevelFragment() {
        return topLevelTabName == null;
    }
}
