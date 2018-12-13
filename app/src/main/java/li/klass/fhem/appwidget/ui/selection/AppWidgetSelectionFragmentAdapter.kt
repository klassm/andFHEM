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

package li.klass.fhem.appwidget.ui.selection

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import li.klass.fhem.R
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetTypeProvider
import li.klass.fhem.appwidget.ui.widget.base.AppWidgetView
import li.klass.fhem.appwidget.ui.widget.base.otherWidgets.OtherWidgetsFragment
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.constants.ResultCodes
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fragments.device.DeviceNameListFragment
import li.klass.fhem.fragments.device.DeviceNameSelectionFragment
import li.klass.fhem.room.list.ui.RoomListFragment
import li.klass.fhem.util.FhemResultReceiver

internal class AppWidgetSelectionFragmentAdapter(fm: FragmentManager, private val widgetTypeProvider: WidgetTypeProvider,
                                                 private val context: Context, private val widgetSize: WidgetSize,
                                                 private val selectionCompletedCallback: SelectionCompletedCallback)
    : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> return devicesFragment()
            1 -> return roomsFragment()
            2 -> return othersFragment()
        }
        throw IllegalStateException("cannot handle position $position")
    }

    override fun getCount(): Int = 3

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return context.getString(R.string.widget_devices)
            1 -> return context.getString(R.string.widget_rooms)
            2 -> return context.getString(R.string.widget_others)
        }
        throw IllegalStateException("cannot handle position $position")
    }


    private fun devicesFragment(): DeviceNameSelectionFragment {
        val bundle = Bundle().apply {
            putSerializable(BundleExtraKeys.DEVICE_FILTER, object : DeviceNameListFragment.DeviceFilter {
                override fun isSelectable(device: FhemDevice): Boolean =
                        widgetTypeProvider.getSupportedDeviceWidgetsFor(widgetSize, device).isNotEmpty()
            })
            putParcelable(BundleExtraKeys.RESULT_RECEIVER, object : FhemResultReceiver() {
                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                    if (resultCode != ResultCodes.SUCCESS || !resultData!!.containsKey(BundleExtraKeys.CLICKED_DEVICE))
                        return

                    val clickedDevice = resultData.getSerializable(BundleExtraKeys.CLICKED_DEVICE) as FhemDevice
                    selectionCompletedCallback.onDeviceSelect(clickedDevice)
                }
            })
            putInt(EMPTY_TEXT_ID, R.string.widgetNoDevices)
        }

        return DeviceNameSelectionFragment().apply {
            arguments = bundle
        }
    }

    private fun roomsFragment(): RoomListFragment {
        val bundle = Bundle()
        bundle.putSerializable(ROOM_SELECTABLE_CALLBACK, object : RoomListFragment.RoomSelectableCallback {
            override fun isRoomSelectable(roomName: String): Boolean =
                    widgetTypeProvider.getSupportedRoomWidgetsFor(widgetSize).isNotEmpty()
        })
        bundle.putSerializable(ON_CLICKED_CALLBACK, object : RoomListFragment.RoomClickedCallback {
            override fun onRoomClicked(roomName: String) {
                selectionCompletedCallback.onRoomSelect(roomName)
            }
        })
        bundle.putInt(EMPTY_TEXT_ID, R.string.widgetNoRooms)

        val fragment = RoomListFragment()
        fragment.arguments = bundle

        return fragment
    }

    private fun othersFragment(): OtherWidgetsFragment {
        val arguments = Bundle()
        arguments.putSerializable(APP_WIDGET_SIZE, widgetSize)
        arguments.putSerializable(ON_CLICKED_CALLBACK, object : OtherWidgetsFragment.OnWidgetClickedCallback {
            override fun onWidgetClicked(widgetView: AppWidgetView) {
                selectionCompletedCallback.onOtherWidgetSelect(widgetView)
            }
        })

        val otherWidgetsFragment = OtherWidgetsFragment()
        otherWidgetsFragment.arguments = arguments

        return otherWidgetsFragment
    }
}
