package li.klass.fhem.adapter.rooms

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.room_device_content.view.*
import li.klass.fhem.R
import li.klass.fhem.domain.core.DeviceType
import li.klass.fhem.domain.core.FhemDevice

class DeviceGroupListAdapter(val devices: List<FhemDevice>,
                             val onClickListener: (FhemDevice) -> Unit,
                             val onLongClickListener: (FhemDevice) -> Boolean
) : RecyclerView.Adapter<DeviceGroupListAdapter.ViewHolder>() {
    override fun getItemCount() = devices.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.room_device_content, parent, false)
        return DeviceGroupListAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(devices[position], onClickListener, onLongClickListener)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(device: FhemDevice, onClickListener: (FhemDevice) -> Unit, onLongClickListener: (FhemDevice) -> Boolean) {
            val adapter = DeviceType.getAdapterFor(device)
            val contentView = adapter.createOverviewView(firstChildOf(itemView.card), device, itemView.context)

            itemView.card.removeAllViews()
            itemView.card.addView(contentView)
            itemView.setOnClickListener { onClickListener(device) }
            itemView.setOnLongClickListener { onLongClickListener(device) }
        }

        private fun firstChildOf(layout: CardView) = when (layout.childCount) {
            0 -> null
            else -> layout.getChildAt(0)
        }
    }
}