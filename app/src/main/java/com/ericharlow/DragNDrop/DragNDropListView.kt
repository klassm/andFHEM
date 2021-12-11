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
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.ListView

class DragNDropListView(context: Context?, attrs: AttributeSet?) : ListView(context, attrs) {
    var mDragMode = false
    var mStartPosition = 0
    var mEndPosition = 0
    var mDragPointOffset //Used to adjust drag view location
            = 0
    var mDragView: ImageView? = null
    var mGestureDetector: GestureDetector? = null
    var mDropListener: DropListener? = null
    var mRemoveListener: RemoveListener? = null
    var mDragListener: DragListener? = null
    fun setDropListener(l: DropListener?) {
        mDropListener = l
    }

    fun setRemoveListener(l: RemoveListener?) {
        mRemoveListener = l
    }

    fun setDragListener(l: DragListener?) {
        mDragListener = l
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        val x = event.x.toInt()
        val y = event.y.toInt()
        if (action == MotionEvent.ACTION_DOWN && x < this.width / 4) {
            mDragMode = true
        }
        if (!mDragMode) return super.onTouchEvent(event)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mStartPosition = pointToPosition(x, y)
                if (mStartPosition != INVALID_POSITION) {
                    val mItemPosition = mStartPosition - firstVisiblePosition
                    mDragPointOffset = y - getChildAt(mItemPosition).top
                    mDragPointOffset -= event.rawY.toInt() - y
                    startDrag(mItemPosition, y)
                    drag(0, y) // replace 0 with x if desired
                }
            }
            MotionEvent.ACTION_MOVE -> {
                drag(0, y) // replace 0 with x if desired
                val currentPosition = pointToPosition(x, y)
                if (currentPosition == lastVisiblePosition) {
                    smoothScrollToPosition(currentPosition + 1)
                } else if (currentPosition == firstVisiblePosition) {
                    smoothScrollToPosition(currentPosition - 1)
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                mDragMode = false
                mEndPosition = pointToPosition(x, y)
                stopDrag(mStartPosition - firstVisiblePosition)
                if (mDropListener != null && mStartPosition != INVALID_POSITION && mEndPosition != INVALID_POSITION) mDropListener!!.onDrop(
                    mStartPosition,
                    mEndPosition
                )
            }
            else -> {
                mDragMode = false
                mEndPosition = pointToPosition(x, y)
                stopDrag(mStartPosition - firstVisiblePosition)
                if (mDropListener != null && mStartPosition != INVALID_POSITION && mEndPosition != INVALID_POSITION) mDropListener!!.onDrop(
                    mStartPosition,
                    mEndPosition
                )
            }
        }
        return true
    }

    // move the drag view
    private fun drag(x: Int, y: Int) {
        if (mDragView != null) {
            val layoutParams = mDragView!!.layoutParams as WindowManager.LayoutParams
            layoutParams.x = x
            layoutParams.y = y - mDragPointOffset
            val mWindowManager = context
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
            mWindowManager.updateViewLayout(mDragView, layoutParams)
            if (mDragListener != null) mDragListener!!.onDrag(
                x,
                y,
                null
            ) // change null to "this" when ready to use
        }
    }

    // enable the drag view for dragging
    private fun startDrag(itemIndex: Int, y: Int) {
        stopDrag(itemIndex)
        val item = getChildAt(itemIndex) ?: return
        item.isDrawingCacheEnabled = true
        if (mDragListener != null) mDragListener!!.onStartDrag(item)

        // Create a copy of the drawing cache so that it does not get recycled
        // by the framework when the list tries to clean up memory
        val bitmap = Bitmap.createBitmap(item.drawingCache)
        val mWindowParams = WindowManager.LayoutParams()
        mWindowParams.gravity = Gravity.TOP
        mWindowParams.x = 0
        mWindowParams.y = y - mDragPointOffset
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        mWindowParams.flags = (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        mWindowParams.format = PixelFormat.TRANSLUCENT
        mWindowParams.windowAnimations = 0
        val context = context
        val v = ImageView(context)
        v.setImageBitmap(bitmap)
        val mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager.addView(v, mWindowParams)
        mDragView = v
    }

    // destroy drag view
    private fun stopDrag(itemIndex: Int) {
        if (mDragView != null) {
            if (mDragListener != null) mDragListener!!.onStopDrag(getChildAt(itemIndex))
            mDragView!!.visibility = GONE
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.removeView(mDragView)
            mDragView!!.setImageDrawable(null)
            mDragView = null
        }
    }

    override fun setAdapter(adapter: ListAdapter) {
        super.setAdapter(adapter)
        if (adapter is DragNDropAdapter<*>) {
            val dragNDropAdapter = adapter
            setDragListener(dragNDropAdapter)
            setDropListener(dragNDropAdapter)
        }
    }

    companion object {
        private val TAG = DragNDropListView::class.java.name
    }
}