package li.klass.fhem.adapter.rooms

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.room_device_content.view.*
import kotlinx.android.synthetic.main.room_device_group.view.*
import li.klass.fhem.R
import li.klass.fhem.adapter.rooms.ViewableElementsCalculator.Element
import li.klass.fhem.domain.core.DeviceType
import li.klass.fhem.domain.core.FhemDevice
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.LoggerFactory

class DeviceGroupAdapter(
        private val elements: List<Element>,
        private val onClickListener: (FhemDevice) -> Unit,
        private val onLongClickListener: (FhemDevice) -> Boolean
) : RecyclerView.Adapter<DeviceGroupAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = elements[position]
        when (holder) {
            is ViewHolder.ForDevice -> holder.bind((element as Element.Device).device, onClickListener, onLongClickListener)
            is ViewHolder.ForGroup -> holder.bind((element as Element.Group).group)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (elements[position]) {
            is Element.Group -> TYPE_GROUP
            is Element.Device -> TYPE_DEVICE
        }
    }

    override fun getItemCount() = elements.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        fun inflateWith(resource: Int) = LayoutInflater.from(parent.context).inflate(resource, parent, false)

        LOGGER.info("onCreateViewHolder, viewType = $viewType")
        return when (viewType) {
            TYPE_GROUP -> ViewHolder.ForGroup(inflateWith(R.layout.room_device_group))
            TYPE_DEVICE -> ViewHolder.ForDevice(inflateWith(R.layout.room_device_content))
            else -> throw RuntimeException("invalid type: " + viewType)
        }
    }


    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class ForGroup(view: View) : ViewHolder(view) {
            fun bind(parent: String) {
                val stopWatch = StopWatch()
                stopWatch.start()

                val layoutParams = itemView.getLayoutParams() as StaggeredGridLayoutManager.LayoutParams
                layoutParams.isFullSpan = true
                itemView.name.text = parent
                LOGGER.info("bind - parent=$parent, took=${stopWatch.time}")
            }
        }

        class ForDevice(view: View) : ViewHolder(view) {
            fun bind(device: FhemDevice, onClickListener: (FhemDevice) -> Unit, onLongClickListener: (FhemDevice) -> Boolean) {
                val stopWatch = StopWatch()
                stopWatch.start()

                val adapter = DeviceType.getAdapterFor(device)

                LOGGER.info("bind - getAdapterFor device=${device.name}, time=${stopWatch.time}")

                val contentView = adapter.createOverviewView(firstChildOf(itemView.card), device, itemView.context)

                LOGGER.info("bind - creating view for device=${device.name}, time=${stopWatch.time}")

                itemView.card.removeAllViews()
                itemView.card.addView(contentView)

                LOGGER.info("bind - adding content view device=${device.name}, time=${stopWatch.time}")

                itemView.setOnClickListener { onClickListener(device) }
                itemView.setOnLongClickListener { onLongClickListener(device) }

                LOGGER.info("bind - finished device=${device.name}, time=${stopWatch.time}")
            }

            private fun firstChildOf(layout: CardView) = when (layout.childCount) {
                0 -> null
                else -> layout.getChildAt(0)
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DeviceGroupAdapter::class.java)
        private val TYPE_GROUP = 1
        private val TYPE_DEVICE = 2
    }
}
