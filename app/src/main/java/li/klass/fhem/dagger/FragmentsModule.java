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

package li.klass.fhem.dagger;

import dagger.Module;
import li.klass.fhem.appwidget.view.widget.base.otherWidgets.OtherWidgetsFragment;
import li.klass.fhem.fragments.AllDevicesFragment;
import li.klass.fhem.fragments.ConnectionDetailFragment;
import li.klass.fhem.fragments.ConnectionListFragment;
import li.klass.fhem.fragments.ConversionFragment;
import li.klass.fhem.fragments.FavoritesFragment;
import li.klass.fhem.fragments.FloorplanFragment;
import li.klass.fhem.fragments.PremiumFragment;
import li.klass.fhem.fragments.RoomDetailFragment;
import li.klass.fhem.fragments.RoomListFragment;
import li.klass.fhem.fragments.SendCommandFragment;
import li.klass.fhem.fragments.TimerDetailFragment;
import li.klass.fhem.fragments.TimerListFragment;
import li.klass.fhem.fragments.WebViewFragment;
import li.klass.fhem.fragments.core.DeviceDetailFragment;
import li.klass.fhem.fragments.device.DeviceNameListFragment;
import li.klass.fhem.fragments.device.DeviceNameListNavigationFragment;
import li.klass.fhem.fragments.device.DeviceNameSelectionFragment;
import li.klass.fhem.fragments.device.DeviceNameSelectionNavigationFragment;
import li.klass.fhem.fragments.weekprofile.FromToWeekProfileFragment;
import li.klass.fhem.fragments.weekprofile.IntervalWeekProfileFragment;

@Module(complete = false,
        injects = {
                FavoritesFragment.class,
                RoomListFragment.class,
                AllDevicesFragment.class,
                ConversionFragment.class,
                DeviceDetailFragment.class,
                DeviceDetailFragment.class,
                FromToWeekProfileFragment.class,
                IntervalWeekProfileFragment.class,
                FloorplanFragment.class,
                PremiumFragment.class,
                RoomDetailFragment.class,
                SendCommandFragment.class,
                DeviceNameListFragment.class,
                DeviceNameSelectionFragment.class,
                DeviceNameListNavigationFragment.class,
                TimerListFragment.class,
                TimerDetailFragment.class,
                ConnectionListFragment.class,
                ConnectionDetailFragment.class,
                WebViewFragment.class,
                OtherWidgetsFragment.class,
                DeviceNameSelectionNavigationFragment.class
        })
public class FragmentsModule {
}
