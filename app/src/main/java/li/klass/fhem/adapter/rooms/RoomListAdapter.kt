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

package li.klass.fhem.adapter.rooms

import android.content.Context
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import li.klass.fhem.R
import li.klass.fhem.adapter.ListDataAdapter
import li.klass.fhem.settings.SettingsKeys.SHOW_HIDDEN_DEVICES
import org.slf4j.LoggerFactory
import java.util.*

class RoomListAdapter(
        context: Context,
        resource: Int,
        data: List<String>,
        val hiddenRooms: Set<String>
) : ListDataAdapter<String>(context, resource, data, CASE_INSENSITIVE_COMPARATOR) {

    private var selectedRoom: String? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertedView = convertView
        val roomName = getItem(position) as String

        if (convertedView == null) {
            convertedView = inflater.inflate(resource, null)
        }

        assert(convertedView != null)

        val roomNameTextView = convertedView!!.findViewById<View>(R.id.roomName) as TextView
        roomNameTextView.text = roomName

        convertedView.tag = roomName

        val backgroundColor = if (roomName == selectedRoom) R.color.android_green else android.R.color.transparent
        convertedView.setBackgroundColor(ContextCompat.getColor(context, backgroundColor))

        return convertedView
    }

    fun updateData(newData: MutableList<String>?, selectedRoom: String?) {
        if (newData == null) return

        setSelectedRoom(selectedRoom)

        val preferences = getDefaultSharedPreferences(context)
        val showHiddenDevices = preferences.getBoolean(SHOW_HIDDEN_DEVICES, false)

        val newRooms = when (showHiddenDevices) {
            true -> newData
            else -> newData.filterNot { hiddenRooms.contains(it) }
        }

        updateData(newRooms)
    }

    private fun setSelectedRoom(selectedRoom: String?) {
        LOG.info("set selected room to {}", selectedRoom)
        this.selectedRoom = selectedRoom
    }

    override fun doSort(): Boolean = true

    companion object {
        val CASE_INSENSITIVE_COMPARATOR: Comparator<String> = Comparator { lhs, rhs -> lhs.toLowerCase().compareTo(rhs.toLowerCase()) }

        private val LOG = LoggerFactory.getLogger(RoomListAdapter::class.java)
    }
}
