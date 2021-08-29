package li.klass.fhem.devices.ui

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import li.klass.fhem.R
import li.klass.fhem.adapter.ListDataAdapter
import li.klass.fhem.domain.core.FhemDevice

class DeviceNameAdapter(context: Context, devices: List<FhemDevice>) :
    ListDataAdapter<FhemDevice>(context, devices) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertedView = convertView
        val device = getItem(position) as FhemDevice

        if (convertedView == null) {
            convertedView = inflater.inflate(R.layout.device_name_item, null)
        }

        convertedView?.findViewById<TextView>(R.id.deviceName)?.apply {
            text = device.aliasOrName
            tag = device
        }

        return convertedView!!
    }
}

object DeviceNameSelectionAlert {
    fun show(context: Context, devices: List<FhemDevice>, listener: (device: FhemDevice?) -> Unit) {
        val adapter = DeviceNameAdapter(context, devices)
        AlertDialog.Builder(context, R.style.alertDialog).setCancelable(true)
                .setTitle(R.string.chooseDevice).setAdapter(adapter) { dialog, which ->
                    dialog?.dismiss()
                    listener(adapter.getItem(which) as FhemDevice)
                }.apply {
                    setNegativeButton(R.string.cancelButton) { dialog, _ ->
                        dialog.dismiss()
                        listener(null)
                    }
                }.create().show()
    }
}