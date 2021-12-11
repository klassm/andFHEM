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

package li.klass.fhem.widget.deviceFunctionality

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import com.ericharlow.DragNDrop.DragNDropListView
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.domain.core.DeviceFunctionality
import li.klass.fhem.settings.SettingsKeys.DEVICE_TYPE_FUNCTIONALITY_ORDER_INVISIBLE
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.widget.deviceFunctionality.DeviceFunctionalityOrderAdapter.OrderAction
import li.klass.fhem.widget.deviceFunctionality.DeviceFunctionalityOrderAdapter.OrderActionListener
import org.apache.pig.impl.util.ObjectSerializer
import javax.inject.Inject

class DeviceFunctionalityOrderPreference(context: Context, attrs: AttributeSet) :
    Preference(context, attrs) {

    @Inject
    lateinit var applicationProperties: ApplicationProperties
    private var wrappedDevices = ArrayList<DeviceFunctionalityPreferenceWrapper>()

    init {
        inject(context)
    }


    private fun inject(context: Context) {
        ((context as Activity).application as AndFHEMApplication).daggerComponent.inject(this)
    }

    override fun onClick() {
        AlertDialog.Builder(context).apply {
            setTitle(R.string.settingsDeviceFunctionalityOrder)
            setView(createView())
            setCancelable(true)
            setPositiveButton(R.string.okButton) { dialog, _ ->
                save()
                dialog.dismiss()
            }
            setNegativeButton(R.string.cancelButton) { dialog, _ -> dialog.cancel() }
        }.create().show()
    }

    private fun createView(): View {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.device_type_order_layout, null)

        val deviceTypeListView = view.findViewById(android.R.id.list) as DragNDropListView

        deviceTypeListView.adapter = DeviceFunctionalityOrderAdapter(
            context,
            wrappedDevices,
            listener = object : OrderActionListener {
                override fun deviceTypeReordered(
                    wrapper: DeviceFunctionalityPreferenceWrapper,
                    action: OrderAction
                ) {
                    val currentPosition = wrappedDevices.indexOf(wrapper)

                    if (action == OrderAction.VISIBILITY_CHANGE) {
                        wrappedDevices[currentPosition].invertVisibility()
                    }

                    callChangeListener(wrappedDevices)
                    saveVisibleDevices()
                    saveInvisibleDevices()
                }
            })

        return view
    }

    override fun onSetInitialValue(value: Any?) {
        super.onSetInitialValue(value)

        val deviceTypeHolder = DeviceGroupHolder(applicationProperties)
        val visible = deviceTypeHolder.getVisible(context)
        val invisible = deviceTypeHolder.getInvisible(context)

        this.wrappedDevices = ArrayList(wrapList(visible, true) + wrapList(invisible, false))
    }

    private fun wrapList(
        toWrap: List<DeviceFunctionality>, isVisible: Boolean
    ): List<DeviceFunctionalityPreferenceWrapper> =
            toWrap.map { DeviceFunctionalityPreferenceWrapper(it, isVisible) }

    private fun save() {
        saveVisibleDevices()
        saveInvisibleDevices()
    }

    private fun saveVisibleDevices() {
        val visibleDevices = wrappedDevices.filter { it.isVisible }
        val toPersist = unwrapDeviceTypes(visibleDevices)
        if (shouldPersist()) persistString(ObjectSerializer.serialize(toPersist))

    }

    private fun saveInvisibleDevices() {
        val invisibleDevices = wrappedDevices.filter { !it.isVisible }
        val toPersist = unwrapDeviceTypes(invisibleDevices)
        if (shouldPersist()) {
            sharedPreferences.edit()
                    .putString(DEVICE_TYPE_FUNCTIONALITY_ORDER_INVISIBLE, ObjectSerializer.serialize(toPersist))
                    .apply()
        }
    }

    private fun unwrapDeviceTypes(toUnwrap: List<DeviceFunctionalityPreferenceWrapper>): Array<DeviceFunctionality> =
            toUnwrap.map { it.deviceFunctionality }.toTypedArray()
}
