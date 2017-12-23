/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.appwidget.ui.widget.base.otherWidgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.LinearLayout
import android.widget.ListView
import com.google.common.base.Function
import com.google.common.collect.Iterables.transform
import com.google.common.collect.Lists.newArrayList
import li.klass.fhem.R
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetType
import li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_SIZE
import li.klass.fhem.constants.BundleExtraKeys.ON_CLICKED_CALLBACK
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.fragments.core.BaseFragment
import org.apache.commons.lang3.tuple.Pair
import java.io.Serializable

class OtherWidgetsFragment : BaseFragment() {

    private var widgetSize: WidgetSize? = null
    private var widgetClickedCallback: OnWidgetClickedCallback? = null

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)

        widgetSize = args?.getSerializable(APP_WIDGET_SIZE) as WidgetSize
        widgetClickedCallback = args.getSerializable(ON_CLICKED_CALLBACK) as OnWidgetClickedCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.other_widgets_list, container, false)

        val listView = view.findViewById<View>(R.id.list) as ListView
        listView.onItemClickListener = OnItemClickListener { _, myView, _, _ ->
            val widgetType = myView.tag as WidgetType
            this@OtherWidgetsFragment.onClick(widgetType)
        }

        val adapter = OtherWidgetsAdapter(activity)
        listView.adapter = adapter

        val emptyView = view.findViewById<View>(R.id.emptyView) as LinearLayout
        fillEmptyView(emptyView, R.string.widgetNoOther, container!!)

        return view
    }

    private fun onClick(type: WidgetType) {
        if (widgetClickedCallback != null) {
            widgetClickedCallback!!.onWidgetClicked(type)
        }
    }

    override fun update(refresh: Boolean) {
        if (view == null) return

        val listView = view!!.findViewById<View>(R.id.list) as ListView
        val adapter = listView.adapter as OtherWidgetsAdapter

        val widgets = WidgetType.getOtherWidgetsFor(widgetSize!!)
        val values = newArrayList(transform(widgets, Function<WidgetType, Pair<WidgetType, String>> { widgetType -> Pair.of(widgetType, activity!!.getString(widgetType!!.widgetView.getWidgetName())) }))

        if (values.isEmpty()) {
            showEmptyView()
        } else {
            hideEmptyView()
        }

        adapter.updateData(values)
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    interface OnWidgetClickedCallback : Serializable {
        fun onWidgetClicked(widgetType: WidgetType)
    }
}
