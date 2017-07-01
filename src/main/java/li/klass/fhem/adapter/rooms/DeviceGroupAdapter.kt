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
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.LoggerFactory

class DeviceGroupAdapter(
        private val parents: List<String>,
        private val roomDeviceList: RoomDeviceList,
        private val onClickListener: (FhemDevice) -> Unit,
        private val onLongClickListener: (FhemDevice) -> Boolean
) : RecyclerView.Adapter<DeviceGroupAdapter.ViewHolder>() {

    val sharedPool = RecyclerView.RecycledViewPool()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val parent = parents[position]
        val devices = roomDeviceList.getDevicesOfFunctionality(parent).sortedBy { it.aliasOrName.toLowerCase() }
        holder.bind(parent, devices, onClickListener, onLongClickListener, sharedPool)
    }

    override fun getItemCount() = parents.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        LOGGER.info("onCreateViewHolder, viewType = $viewType")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.room_device_group, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(parent: String, devices: List<FhemDevice>, onClickListener: (FhemDevice) -> Unit, onLongClickListener: (FhemDevice) -> Boolean, sharedPool: RecyclerView.RecycledViewPool) {

            val stopWatch = StopWatch()
            stopWatch.start()
            itemView.name.text = parent
            itemView.content.layoutManager = StaggeredGridLayoutManager(getNumberOfColumns(), StaggeredGridLayoutManager.VERTICAL)
            itemView.content.recycledViewPool = sharedPool
            itemView.content.adapter = DeviceGroupListAdapter(devices, onClickListener, onLongClickListener)
            LOGGER.info("bind - parent=$parent, took=${stopWatch.time}")
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

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DeviceGroupAdapter::class.java)
    }
}
