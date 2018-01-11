package li.klass.fhem.appwidget.ui.widget.base.otherWidgets

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.simple_list_item.view.*
import li.klass.fhem.R
import li.klass.fhem.appwidget.ui.widget.base.AppWidgetView

class OtherWidgetsAdapter(elements: List<AppWidgetView>, val widgetClickedCallback: OtherWidgetsFragment.OnWidgetClickedCallback)
    : RecyclerView.Adapter<OtherWidgetsAdapter.ViewHolder>() {
    private val sortedElements = elements.sortedBy { it.getWidgetName() }

    override fun getItemCount() = sortedElements.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.one_line_card, parent, false)
        return ViewHolder(layout, widgetClickedCallback)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sortedElements[position])
    }

    class ViewHolder(val view: View, val widgetClickedCallback: OtherWidgetsFragment.OnWidgetClickedCallback) : RecyclerView.ViewHolder(view) {
        fun bind(appWidgetView: AppWidgetView) {
            view.text.text = view.context.getString(appWidgetView.getWidgetName())
            view.setOnClickListener { widgetClickedCallback.onWidgetClicked(appWidgetView) }
        }
    }
}
