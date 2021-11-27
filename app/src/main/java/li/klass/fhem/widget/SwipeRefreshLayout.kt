package li.klass.fhem.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class SwipeRefreshLayout(context: Context, attrs: AttributeSet?) : SwipeRefreshLayout(
    context, attrs
) {
    private val mTouchSlop: Int
    private var mDownX = 0f
    private var mHorizontalSwipe = false
    private var mChildScrollDelegate: ChildScrollDelegate? = null

    fun setChildScrollDelegate(delegate: ChildScrollDelegate?) {
        mChildScrollDelegate = delegate
    }

    override fun canChildScrollUp(): Boolean {
        return (super.canChildScrollUp()
                || (mChildScrollDelegate != null
                && mChildScrollDelegate!!.canChildScrollUp()))
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = event.x
                mHorizontalSwipe = false
            }
            MotionEvent.ACTION_MOVE -> {
                val eventX = event.x
                val xDiff = Math.abs(eventX - mDownX)
                if (mHorizontalSwipe || xDiff > mTouchSlop) {
                    mHorizontalSwipe = true
                    return false
                }
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    interface ChildScrollDelegate {
        fun canChildScrollUp(): Boolean
    }

    init {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }
}