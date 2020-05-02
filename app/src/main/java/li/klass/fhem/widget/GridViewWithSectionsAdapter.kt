/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 *  server.
 *
 *  Copyright (c) 2012, Matthias Klass or third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by Red Hat Inc.
 *
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 *  for more details.
 *
 *  You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 *  along with this distribution; if not, write to:
 *    Free Software Foundation, Inc.
 *    51 Franklin Street, Fifth Floor
 */
package li.klass.fhem.widget

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

abstract class GridViewWithSectionsAdapter<P, C>(protected val context: Context) : BaseAdapter() {
    private var parentPositions: MutableMap<Int, P>? = null
    private var totalNumberOfItems = 0
    protected val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private var currentRowIndex = 0
    private var currentRowParentIndex = 0
    private var currentRowHeight = 0
    private val currentRowViews: MutableList<View> = mutableListOf()
    var numberOfColumns = -1
        get() = if (field <= 0) 1 else field
        set(numberOfColumns) {
            field = numberOfColumns
            Log.d(TAG, "set grid view to $numberOfColumns columns")
            updateParentPositions()
            notifyDataSetChanged()
        }

    private fun updateParentPositions() {
        val parents = deviceGroupParents
        Log.v(TAG, "updating parent positions for parent count " + parents.size)
        parentPositions = mutableMapOf()
        val numberOfColumns = numberOfColumns
        var currentPosition = 0
        for (parent in parents) {
            parentPositions!![currentPosition] = parent

            // add all the children plus an offset to complete the grid row
            val childCount = getChildrenCountForParent(parent)
            val filledItemsWithinTheRow = childCount % numberOfColumns
            val childOffset = if (filledItemsWithinTheRow == 0) 0 else numberOfColumns - filledItemsWithinTheRow
            currentPosition += childCount + childOffset

            // add the parent row
            currentPosition += numberOfColumns
        }
        totalNumberOfItems = currentPosition
        Log.v(TAG, "found $totalNumberOfItems items")
    }

    override fun getCount(): Int {
        Log.v(TAG, "returning totalNumberOfItems: $totalNumberOfItems")
        return totalNumberOfItems
    }

    override fun getItem(position: Int): Any {
        var parent = getParentForPosition(position)
        if (parent != null) {
            return parent
        }
        val parentPosition = findParentPositionForChildPosition(position)
        parent = parentPositions!![parentPosition]
        val relativeChildPosition = position - parentPosition
        return getChildForParentAndChildPosition(parent, relativeChildPosition) as Any
    }

    fun findParentPositionForChildPosition(flatPosition: Int): Int {
        val keyPositions: Set<Int> = parentPositions!!.keys
        var bestKeyMatch = 0
        var bestKeyDiff = -1
        for (keyPosition in keyPositions) {
            val diff = flatPosition - keyPosition
            if (diff >= 0 && (bestKeyDiff == -1 || diff < bestKeyDiff)) {
                bestKeyDiff = diff
                bestKeyMatch = keyPosition
            }
        }
        return bestKeyMatch
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(flatPosition: Int, view: View, viewGroup: ViewGroup): View? {
        Log.v(TAG, "drawing flatPosition $flatPosition/$totalNumberOfItems")
        return try {
            val parentBasePosition = getParentBasePosition(flatPosition)
            if (parentBasePosition != -1) {
                val parent = parentPositions!![parentBasePosition]
                val parentOffset = flatPosition - parentBasePosition
                getParentView(parent, parentOffset, view, viewGroup)
            } else {
                val parentPosition = findParentPositionForChildPosition(flatPosition)
                val parent = parentPositions!![parentPosition]
                val relativeChildPosition = flatPosition - parentPosition - numberOfColumns
                val child = getChildForParentAndChildPosition(parent, relativeChildPosition)
                val childView = getChildView(parent, parentPosition, child, view, viewGroup)
                if (numberOfColumns > 1) {
                    updateChildrenRowHeight(numberOfColumns, parentPosition, relativeChildPosition, childView)
                }
                childView
            }
        } catch (e: Exception) {
            Log.e(TAG, "error occurred", e)
            null
        }
    }

    private fun updateChildrenRowHeight(columns: Int, parentIndex: Int, childOffset: Int, childView: View) {
        val rowIndex = getRowForChildOffset(columns, childOffset)
        if (currentRowParentIndex != parentIndex || currentRowIndex != rowIndex) {
            currentRowViews.clear()
            currentRowHeight = 0
        }
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        childView.measure(widthMeasureSpec, heightMeasureSpec)
        val measuredHeight = childView.measuredHeight
        currentRowViews.add(childView)
        if (measuredHeight > currentRowHeight) {
            currentRowHeight = measuredHeight
        }
        setHeightForViews(currentRowHeight, currentRowViews)
        currentRowIndex = rowIndex
        currentRowParentIndex = parentIndex
    }

    private fun getRowForChildOffset(columns: Int, childOffset: Int): Int {
        return childOffset / columns
    }

    private fun setHeightForViews(height: Int, views: List<View>) {
        for (view in views) {
            var layoutParams = view.layoutParams
            if (layoutParams == null) {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            layoutParams.height = height
            view.layoutParams = layoutParams
        }
    }

    protected fun getParentForPosition(position: Int): P? {
        val basePosition = getParentBasePosition(position)
        return if (basePosition == -1) null else parentPositions!![basePosition]
    }

    protected fun getParentBasePosition(position: Int): Int {
        val numberOfColumns = numberOfColumns
        for (key in parentPositions!!.keys) {
            if (key <= position && key + numberOfColumns > position) {
                return key
            }
        }
        return -1
    }

    protected fun updateData() {
        updateParentPositions()
        notifyDataSetChanged()
    }

    fun findOriginalParentPosition(flatPosition: Int): Int {
        var parent = getParentForPosition(flatPosition)
        parent = if (parent != null) {
            parentPositions!![flatPosition]
        } else {
            parentPositions!![findParentPositionForChildPosition(flatPosition)]
        }
        val parents = deviceGroupParents
        for (i in parents.indices) {
            if (parents[i] == parent) {
                return i
            }
        }
        return -1
    }

    override fun isEmpty(): Boolean {
        return totalNumberOfItems == 0
    }

    abstract fun getChildForParentAndChildPosition(parent: P?, childPosition: Int): C
    protected abstract fun getChildrenCountForParent(parent: P): Int
    protected abstract fun getParentView(parent: P?, parentOffset: Int, view: View?, viewGroup: ViewGroup?): View
    protected abstract fun getChildView(parent: P?, parentPosition: Int, child: C, view: View?, viewGroup: ViewGroup?): View
    abstract val deviceGroupParents: List<P>
    abstract fun getRequiredColumnWidth(): Int

    companion object {
        val TAG = GridViewWithSectionsAdapter::class.java.name
    }

}