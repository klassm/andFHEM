package li.klass.fhem.fcm.history.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import li.klass.fhem.R
import li.klass.fhem.databinding.FcmHistoryChangeRowBinding
import li.klass.fhem.databinding.FcmHistoryUpdatesItemBinding
import li.klass.fhem.fcm.history.data.FcmHistoryService
import li.klass.fhem.util.DateFormatUtil

class FcmUpdatesAdapter(elements: List<FcmHistoryService.SavedChange>) : RecyclerView.Adapter<FcmUpdatesAdapter.ViewHolder>() {
    private var elements: MutableList<FcmHistoryService.SavedChange> = elements.toMutableList()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(elements[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.fcm_history_updates_item, parent, false)
    )

    override fun getItemCount() = elements.size

    fun updateWith(newElements: List<FcmHistoryService.SavedChange>) {
        elements.clear()
        elements.addAll(newElements)
        notifyDataSetChanged()
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("InflateParams")
        fun bind(change: FcmHistoryService.SavedChange) {
            val inflater = LayoutInflater.from(view.context)
            val binding = FcmHistoryUpdatesItemBinding.bind(view)
            binding.apply {
                sendTime.text = DateFormatUtil.ANDFHEM_DATE_TIME_FORMAT.print(change.time)
                device.text = change.deviceName
                receiveTime.apply {
                    if (change.receiveTime != null) {
                        visibility = View.VISIBLE
                        text = String.format(
                            context.getString(R.string.fcm_history_received),
                            DateFormatUtil.ANDFHEM_DATE_TIME_FORMAT.print(change.receiveTime)
                        )
                    } else {
                        visibility = View.GONE
                    }
                }

                changes.removeAllViews()

                change.changes.sortedBy { it.first }
                    .map {
                        val row = FcmHistoryChangeRowBinding.inflate(inflater, null, false)
                        row.attribute.text = it.first
                        row.value.text = it.second
                        row
                    }.forEach { changes.addView(it.root) }
            }
        }
    }
}