package li.klass.fhem.fcm.history.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fcm_history_messages_item.view.*
import li.klass.fhem.R
import li.klass.fhem.fcm.history.data.FcmHistoryService
import li.klass.fhem.util.DateFormatUtil
import org.apache.commons.lang3.StringUtils

class FcmMessagesAdapter(elements: List<FcmHistoryService.SavedMessage>) : RecyclerView.Adapter<FcmMessagesAdapter.ViewHolder>() {
    private var elements: MutableList<FcmHistoryService.SavedMessage> = elements.toMutableList()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(elements[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.fcm_history_messages_item, parent, false)
    )

    override fun getItemCount() = elements.size

    fun updateWith(newElements: List<FcmHistoryService.SavedMessage>) {
        elements.clear()
        elements.addAll(newElements)
        notifyDataSetChanged()
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(message: FcmHistoryService.SavedMessage) {
            view.apply {
                sendTime.text = DateFormatUtil.ANDFHEM_DATE_TIME_FORMAT.print(message.time)
                receiveTime.apply {
                    if (message.receiveTime != null) {
                        visibility = View.VISIBLE
                        text = String.format(context.getString(R.string.fcm_history_received), DateFormatUtil.ANDFHEM_DATE_TIME_FORMAT.print(message.receiveTime))
                    } else {
                        visibility = View.GONE
                    }
                }
                if (message.ticker == message.title) {
                    ticker.visibility = View.GONE
                } else {
                    setText(ticker, message.ticker)
                }
                setText(title, message.title)
                setText(text, message.text)
            }
        }

        private fun setText(textView: TextView, content: String) {
            val nullableContent: String? = StringUtils.trimToNull(content)
            if (nullableContent == null) {
                textView.visibility = View.GONE
                textView.text = ""
            } else {
                textView.visibility = View.VISIBLE
                textView.text = nullableContent
            }
        }
    }
}