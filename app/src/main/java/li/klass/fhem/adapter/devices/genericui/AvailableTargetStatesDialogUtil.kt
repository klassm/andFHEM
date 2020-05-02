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
package li.klass.fhem.adapter.devices.genericui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.*
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.SetListEntry

object AvailableTargetStatesDialogUtil {
    private val handlersWithoutNoArg: List<SetListTargetStateHandler<FhemDevice>> = listOf(
            RGBTargetStateHandler(),
            GroupSetListTargetStateHandler(),
            SliderSetListTargetStateHandler(),
            TimeTargetStateHandler(),
            TextFieldTargetStateHandler(),
            MultipleSetListTargetStateHandler(),
            SpecialButtonSecondsHandler(),
            SpecialButtonHandler(),
            DateTimeSetListEntryTargetStateHandler()
    )
    private val handlers: List<SetListTargetStateHandler<FhemDevice>> =
            handlersWithoutNoArg + listOf(NoArgSetListTargetStateHandler()) // must be last entry

    fun <D : FhemDevice> showSwitchOptionsMenu(context: Context, device: D, callback: OnTargetStateSelectedCallback<FhemDevice>) {
        val contextMenu = AlertDialog.Builder(context, R.style.alertDialog)
        contextMenu.setTitle(context.resources.getString(R.string.switchDevice))
        val setList = device.setList
        val setOptions = setList.sortedKeys
        val eventMapOptions: Array<String> = device.availableTargetStatesEventMapTexts
        val clickListener = DialogInterface.OnClickListener { dialog, position ->
            val option = setOptions[position]
            handleSelectedOption(context, device, setList[option, true], callback)
            dialog.dismiss()
        }
        val adapter: ArrayAdapter<String> = SetListArrayAdapter<D>(context, eventMapOptions, setOptions, device)
        contextMenu.setAdapter(adapter, clickListener)
        contextMenu.show()
    }

    fun <D : FhemDevice?> showSwitchOptionsMenuFor(context: Context?, device: D, callback: OnTargetStateSelectedCallback<FhemDevice>) {
        val entry = device!!.setList["state", true]
        handleSelectedOption(context, device, entry, callback)
    }

    fun <D : FhemDevice?> handleSelectedOption(context: Context?, device: D, option: SetListEntry, callback: OnTargetStateSelectedCallback<FhemDevice>): Boolean {
        for (handler in handlers) {
            if (handler.canHandle(option)) {
                handler.handle(option, context, device, callback)
                return true
            }
        }
        return false
    }

    private class SetListArrayAdapter<D : FhemDevice?> constructor(context: Context, eventMapOptions: Array<String>, private val setOptions: List<String>, private val device: D)
        : ArrayAdapter<String>(context, R.layout.list_item_with_arrow, eventMapOptions) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: View.inflate(context, R.layout.list_item_with_arrow, null)
            val textView = view!!.findViewById<TextView>(R.id.text)
            val imageView = view.findViewById<ImageView>(R.id.image)
            textView.text = getItem(position)
            val setOption = setOptions[position]
            val setList = device!!.setList
            val setListEntry = setList[setOption, true]
            imageView.visibility = if (requiresAdditionalInformation(setListEntry)) View.VISIBLE else View.GONE
            return view
        }

        private fun requiresAdditionalInformation(entry: SetListEntry): Boolean =
                handlersWithoutNoArg.any {
                    it.canHandle(entry)
                }
    }
}