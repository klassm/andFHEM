package li.klass.fhem.appwidget.ui.selection.other

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.simple_list_item.view.*
import li.klass.fhem.R
import li.klass.fhem.appwidget.ui.widget.base.AppWidgetView

class OtherWidgetsAdapter(elements: List<AppWidgetView>, val callback: (view: AppWidgetView) -> Unit)
    : androidx.recyclerview.widget.RecyclerView.Adapter<OtherWidgetsAdapter.ViewHolder>() {
    private val sortedElements = elements.sortedBy { it.getWidgetName() }

    override fun getItemCount() = sortedElements.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.one_line_card, parent, false)
        return ViewHolder(layout, callback)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sortedElements[position])
    }

    class ViewHolder(val view: View, val widgetClickedCallback: (view: AppWidgetView) -> Unit) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun bind(appWidgetView: AppWidgetView) {
            view.text.text = view.context.getString(appWidgetView.getWidgetName())
            view.setOnClickListener { widgetClickedCallback(appWidgetView) }
        }
    }
}
