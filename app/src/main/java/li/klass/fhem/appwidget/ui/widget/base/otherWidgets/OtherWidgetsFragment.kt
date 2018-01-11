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
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.other_widgets_list.view.*
import li.klass.fhem.R
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetTypeProvider
import li.klass.fhem.appwidget.ui.widget.base.AppWidgetView
import li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_SIZE
import li.klass.fhem.constants.BundleExtraKeys.ON_CLICKED_CALLBACK
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.fragments.core.BaseFragment
import java.io.Serializable
import javax.inject.Inject

class OtherWidgetsFragment : BaseFragment() {

    lateinit var widgetSize: WidgetSize
    lateinit var widgetClickedCallback: OnWidgetClickedCallback

    @Inject
    lateinit var widgetTypeProvider: WidgetTypeProvider

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)

        widgetSize = args?.getSerializable(APP_WIDGET_SIZE) as WidgetSize
        widgetClickedCallback = args.getSerializable(ON_CLICKED_CALLBACK) as OnWidgetClickedCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.other_widgets_list, container, false)
        val items = widgetTypeProvider.getOtherWidgetsFor(widgetSize)
        view.list.layoutManager = LinearLayoutManager(activity)
        view.list.adapter = OtherWidgetsAdapter(items, widgetClickedCallback).apply { notifyDataSetChanged() }

        val emptyView = view.findViewById<View>(R.id.emptyView) as LinearLayout
        fillEmptyView(emptyView, R.string.widgetNoOther, container!!)
        emptyView.apply { if (items.isEmpty()) visibility = View.VISIBLE }

        return view
    }

    override fun update(refresh: Boolean) {
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    interface OnWidgetClickedCallback : Serializable {
        fun onWidgetClicked(widgetView: AppWidgetView)
    }
}
