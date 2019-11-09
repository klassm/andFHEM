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

package li.klass.fhem.adapter.weekprofile

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.google.common.collect.FluentIterable
import com.google.common.collect.Lists.newArrayList
import li.klass.fhem.R
import li.klass.fhem.domain.heating.schedule.DayProfile
import li.klass.fhem.domain.heating.schedule.WeekProfile
import li.klass.fhem.domain.heating.schedule.configuration.HeatingIntervalConfiguration
import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval
import li.klass.fhem.widget.NestedListViewAdapter
import java.util.*

abstract class BaseWeekProfileAdapter<INTERVAL : BaseHeatingInterval<INTERVAL>> internal constructor(
        protected val context: Context) :
        NestedListViewAdapter<DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>, INTERVAL>(
                context) {

    protected var weekProfile: WeekProfile<INTERVAL, *>? = null


    protected val resources: Resources = context.resources
    private var listener: WeekProfileChangedListener? = null

    override fun getChildrenCountForParent(
            parent: DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>): Int {
        return parent.numberOfHeatingIntervals + numberOfAdditionalChildrenForParent
    }

    protected abstract val numberOfAdditionalChildrenForParent: Int

    override fun getChildForParentAndChildPosition(
            parent: DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>,
            childPosition: Int): INTERVAL? {
        return parent.getHeatingIntervalAt(childPosition)
    }

    override fun getParentView(parent: DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>,
                               view: View?, viewGroup: ViewGroup): View {
        val myView = layoutInflater.inflate(R.layout.weekprofile_parent, viewGroup, false)

        val parentTextView = myView.findViewById<TextView>(R.id.parent)
        parentTextView.text = getParentTextFor(parent)

        val button = myView.findViewById<Button>(R.id.copy)
        button.setOnClickListener { showCopyContextMenuFor(parent) }

        return myView
    }

    private fun getParentTextFor(
            profile: DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>?) = when {
        profile != null -> resources.getText(profile.day.stringId).toString()
        else            -> "??"
    }

    private fun showCopyContextMenuFor(target: DayProfile<INTERVAL, *>) {
        val contextMenu = AlertDialog.Builder(context)
        contextMenu.setTitle(context.resources.getString(R.string.switchDevice))
        val parents = parents
        val selectOptions =
                FluentIterable.from(parents).transform { input -> getParentTextFor(input) }.toList()

        val clickListener = DialogInterface.OnClickListener { dialog, position ->
            val source = parents[position]
            if (source.day == target.day) {
                return@OnClickListener
            }

            val heatingIntervals = source.getHeatingIntervals()
            if (heatingIntervals.isEmpty()) {
                return@OnClickListener
            }
            target.replaceHeatingIntervalsWith(heatingIntervals)
            dialog.dismiss()
            notifyWeekProfileChangedListener()
        }
        contextMenu.setAdapter(
                ArrayAdapter(context, android.R.layout.simple_list_item_1, selectOptions),
                clickListener)
        contextMenu.show()
    }

    override fun getParents(): List<DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>> {
        val parents = newArrayList<DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>>()
        if (weekProfile == null) return parents

        val sortedDayProfiles = weekProfile!!.sortedDayProfiles

        parents += sortedDayProfiles
        return parents
    }

    fun updateData(weekProfile: WeekProfile<INTERVAL, *>?) {
        if (weekProfile == null) return
        this.weekProfile = weekProfile
        super.updateData()
    }

    fun registerWeekProfileChangedListener(listener: WeekProfileChangedListener) {
        this.listener = listener
    }

    fun notifyWeekProfileChangedListener() {
        notifyDataSetChanged()
        if (listener != null) {
            weekProfile?.let { listener!!.onWeekProfileChanged(it) }
        }
    }

    fun timeToTimeString(hourOfDay: Int, minuteOfDay: Int): String {
        var myHour = hourOfDay
        val intervalMinutesMustBeDivisibleBy =
                weekProfile!!.configuration.getIntervalMinutesMustBeDivisibleBy()
        var minutes =
                (minuteOfDay + intervalMinutesMustBeDivisibleBy - 1) / intervalMinutesMustBeDivisibleBy * intervalMinutesMustBeDivisibleBy
        if (minutes == 60) minutes = 0

        if (minutes == 0 && minuteOfDay != 0) myHour += 1

        return String.format(Locale.getDefault(), "%02d", myHour) + ":" + String.format(
                Locale.getDefault(), "%02d", minutes)
    }

    fun setDetailTextView(view: View, layoutItemId: Int, currentText: String?,
                          originalText: String?, isNew: Boolean) {
        val layoutItem = view.findViewById<TextView>(layoutItemId)
        layoutItem.text = weekProfile!!.formatTimeForDisplay(currentText ?: "", context)

        if (isNew || originalText == null || currentText == null || originalText != currentText) {
            layoutItem.setTextColor(Color.BLUE)
        }
    }


    interface WeekProfileChangedListener {
        fun onWeekProfileChanged(weekProfile: WeekProfile<*, *>)
    }
}
