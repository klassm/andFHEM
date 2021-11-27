/*
 * Copyright (C) 2010 Eric Harlow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ericharlow.DragNDrop

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ListView
import li.klass.fhem.R
import li.klass.fhem.adapter.ListDataAdapter
import java.util.*

abstract class DragNDropAdapter<T : Comparable<T>?>(context: Context?, data: ArrayList<T>?) :
    ListDataAdapter<T>(
        context!!, data
    ), RemoveListener, DropListener, DragListener {
    override fun onRemove(which: Int) {
        if (which < 0 || which > getData().size) return
        updateData(getData().toMutableList().apply { removeAt(which) })
    }

    override fun onDrop(from: Int, to: Int) {
        Log.e(DragNDropAdapter::class.java.name, "drop from $from to $to")
        val temp = getData()[from]
        updateData(getData().toMutableList().apply {
            removeAt(from)
            add(to, temp)
        })
    }

    override fun onStartDrag(itemView: View?) {
        itemView!!.setBackgroundColor(itemView.context.resources.getColor(R.color.focusedColor))
    }

    override fun onDrag(x: Int, y: Int, listView: ListView?) {}
    override fun onStopDrag(itemView: View?) {
        if (itemView == null) return
        itemView.setBackgroundColor(itemView.context.resources.getColor(android.R.color.transparent))
    }
}