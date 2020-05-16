package li.klass.fhem.util.view

import android.view.View
import android.widget.RemoteViews

fun RemoteViews.setTextViewTextOrHide(viewId: Int, toSet: String?) {
    if (toSet == null) {
        setViewVisibility(viewId, View.GONE)
    } else {
        setViewVisibility(viewId, View.VISIBLE)
        setTextViewText(viewId, toSet)
    }
}