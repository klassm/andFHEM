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
package li.klass.fhem.adapter

import android.content.Context
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.widget.BaseAdapter
import java.util.*

abstract class ListDataAdapter<T : Comparable<T>?> @JvmOverloads constructor(
    val context: Context,
    @Volatile private var contents: MutableList<T>?,
    private val comparator: Comparator<T>? = null
) : BaseAdapter() {

    val inflater: LayoutInflater = LayoutInflater.from(context)

    init {
        sortData()
    }

    open fun updateData(newData: MutableList<T>?) {
        contents = newData

        sortData()
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return if (contents == null) 0 else contents!!.size
    }

    override fun getItem(i: Int): Any {
        return contents!![i] as Any
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    private fun sortData() {
        if (!doSort()) return
        if (contents == null || contents!!.size == 0) return
        if (comparator != null) {
            Collections.sort(contents, comparator)
        } else {
            Collections.sort(contents)
        }
    }

    override fun isEmpty(): Boolean {
        return contents == null || contents!!.size == 0
    }

    fun getData(): List<T> {
        return Collections.unmodifiableList(contents)
    }

    protected open fun doSort(): Boolean {
        return true
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        // Workaround for a silly bug in Android 4
        // see http://code.google.com/p/android/issues/detail?id=22946 for details
        if (observer != null) {
            super.unregisterDataSetObserver(observer)
        }
    }
}