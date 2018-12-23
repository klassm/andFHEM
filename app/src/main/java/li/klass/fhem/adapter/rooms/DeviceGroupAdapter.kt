package li.klass.fhem.adapter.rooms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.room_device_group.view.*
import li.klass.fhem.R
import li.klass.fhem.devices.list.backend.ViewableElementsCalculator.Element
import li.klass.fhem.domain.core.FhemDevice
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.LoggerFactory

class DeviceGroupAdapter(
        elements: List<Element>,
        private val configuration: Configuration
) : RecyclerView.Adapter<DeviceGroupAdapter.ViewHolder>() {

    private var elements: MutableList<Element> = elements.toMutableList()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = elements[position]
        when (holder) {
            is ViewHolder.ForDevice -> holder.bind((element as Element.Device).device, configuration)
            is ViewHolder.ForGroup -> holder.bind((element as Element.Group).group)
        }
    }

    override fun getItemViewType(position: Int): Int = when (elements[position]) {
        is Element.Group -> TYPE_GROUP
        is Element.Device -> TYPE_DEVICE
    }

    override fun getItemCount() = elements.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        fun inflateWith(resource: Int) = LayoutInflater.from(parent.context).inflate(resource, parent, false)

        LOGGER.info("onCreateViewHolder, viewType = $viewType")
        return when (viewType) {
            TYPE_GROUP -> ViewHolder.ForGroup(inflateWith(R.layout.room_device_group))
            TYPE_DEVICE -> ViewHolder.ForDevice(inflateWith(configuration.deviceResourceId))
            else -> throw RuntimeException("invalid type: " + viewType)
        }
    }

    fun updateWith(newElements: List<Element>) {
        elements.clear()
        elements.addAll(newElements)
        notifyDataSetChanged()
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class ForGroup(view: View) : ViewHolder(view) {
            fun bind(parent: String) {
                val stopWatch = StopWatch()
                stopWatch.start()

                val layoutParams = itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
                layoutParams.isFullSpan = true
                itemView.name.text = parent
                LOGGER.info("bind - parent=$parent, took=${stopWatch.time}")
            }
        }

        class ForDevice(view: View) : ViewHolder(view) {
            fun bind(device: FhemDevice, configuration: Configuration) {
                configuration.bind(device, itemView)
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DeviceGroupAdapter::class.java)
        private const val TYPE_GROUP = 1
        private const val TYPE_DEVICE = 2
    }

    data class Configuration(val deviceResourceId: Int,
                             val bind: (FhemDevice, View) -> Unit)
}
