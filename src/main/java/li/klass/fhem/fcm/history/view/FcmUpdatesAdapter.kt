package li.klass.fhem.fcm.history.view

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fcm_history_change_row.view.*
import kotlinx.android.synthetic.main.fcm_history_updates_item.view.*
import li.klass.fhem.R
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
            view.apply {
                time.text = DateFormatUtil.ANDFHEM_DATE_TIME_FORMAT.print(change.time)
                device.text = change.deviceName
                changes.removeAllViews()

                change.changes.sortedBy { it.first }
                        .map {
                            val row = inflater.inflate(R.layout.fcm_history_change_row, null)
                            row.attribute.text = it.first
                            row.value.text = it.second
                            row
                        }.forEach { changes.addView(it) }
            }
        }
    }
}