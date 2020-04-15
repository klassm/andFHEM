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

package li.klass.fhem.appwidget.ui.selection.other

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.other_widgets_list.view.*
import li.klass.fhem.R
import li.klass.fhem.appwidget.ui.selection.WidgetSelectionViewModel
import li.klass.fhem.appwidget.ui.widget.WidgetTypeProvider
import li.klass.fhem.fragments.core.BaseFragment
import javax.inject.Inject

class OtherWidgetsFragment @Inject constructor() : BaseFragment() {

    @Inject
    lateinit var widgetTypeProvider: WidgetTypeProvider

    private val viewModel by activityViewModels<WidgetSelectionViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.other_widgets_list, container, false)
        val items = widgetTypeProvider.getOtherWidgetsFor(viewModel.widgetSize)
        view.list.layoutManager = LinearLayoutManager(activity)
        view.list.adapter = OtherWidgetsAdapter(items) {
            viewModel.otherWidgetClicked.postValue(it)
        }.apply { notifyDataSetChanged() }

        val emptyView = view.findViewById<View>(R.id.emptyView) as LinearLayout
        fillEmptyView(emptyView, R.string.widgetNoOther, container!!)
        emptyView.apply { if (items.isEmpty()) visibility = View.VISIBLE }

        return view
    }

    override suspend fun update(refresh: Boolean) {
    }

    override fun getTitle(context: Context): String? = context.getString(R.string.widget_others)
}
