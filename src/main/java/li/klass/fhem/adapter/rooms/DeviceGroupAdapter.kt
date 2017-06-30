package li.klass.fhem.adapter.rooms

import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.room_device_group.view.*
import li.klass.fhem.R
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList

class DeviceGroupAdapter(
        private val parents: List<String>,
        private val roomDeviceList: RoomDeviceList,
        private val onClickListener: (FhemDevice) -> Unit,
        private val onLongClickListener: (FhemDevice) -> Boolean
) : RecyclerView.Adapter<DeviceGroupAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val parent = parents[position]
        holder.bind(parent, roomDeviceList.getDevicesOfFunctionality(parent), onClickListener, onLongClickListener)
    }

    override fun getItemCount() = parents.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.room_device_group, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(parent: String, devices: List<FhemDevice>, onClickListener: (FhemDevice) -> Unit, onLongClickListener: (FhemDevice) -> Boolean) {
            itemView.name.text = parent
            itemView.content.layoutManager = StaggeredGridLayoutManager(getNumberOfColumns(), StaggeredGridLayoutManager.VERTICAL)
            itemView.content.adapter = DeviceGroupListAdapter(devices, onClickListener, onLongClickListener)
        }

        fun getNumberOfColumns(): Int {
            val displayMetrics = Resources.getSystem().displayMetrics
            val calculated = (dpFromPx(displayMetrics.widthPixels.toFloat()) / 300F).toInt()
            return when {
                calculated < 1 -> 1
                else -> calculated
            }
        }

        private fun dpFromPx(px: Float): Float {
            return px / Resources.getSystem().displayMetrics.density
        }
    }
}
