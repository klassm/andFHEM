package li.klass.fhem.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.Set;

public class NestedListView extends ListView {

    private Drawable childDivider;

    public interface OnExtendableListClickObserver {
        void onItemClick(View view, int parentPosition, int childPosition);
    }

    public NestedListView(Context context) {
        super(context);
    }

    public NestedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!(adapter instanceof NestedListViewAdapter)) {
            throw new RuntimeException("I am expecting a NestedListViewAdapter.");
        }
        super.setAdapter(adapter);
    }

    public NestedListViewAdapter getNestedListViewAdapter() {
        return (NestedListViewAdapter) getAdapter();
    }

    // the method override drawDivider in ListView
    void drawDivider(Canvas canvas, Rect bounds, int childIndex) {
        if (childIndex == -1) return;
        int position = childIndex + getFirstVisiblePosition();

        boolean isParent = getNestedListViewAdapter().isParent(position);
        if (childDivider != null && !isParent) {
            childDivider.setBounds(bounds);
            childDivider.draw(canvas);
        } else if (isParent && getDivider() != null){
            getDivider().setBounds(bounds);
            getDivider().draw(canvas);
        }
    }


    public void setChildDivider(Drawable childDivider) {
        if (childDivider != null && childDivider instanceof ColorDrawable) {
            this.childDivider = childDivider;
        }
    }

    @Override
    public boolean performItemClick(View view, int position, long id) {
        int parentPosition = getNestedListViewAdapter().findOriginalParentPosition(position);
        int childPosition = position - getNestedListViewAdapter().findParentPositionForChildPosition(position) - 1;

        performParentChildItemClick(view, parentPosition, childPosition);
        return super.performItemClick(view, parentPosition, id);
    }

    @SuppressWarnings("unchecked")
    private void performParentChildItemClick(View view, int parentPosition, int childPosition) {
        Set<OnExtendableListClickObserver> parentChildClickObservers = getNestedListViewAdapter().parentChildClickObservers;
        for (OnExtendableListClickObserver parentChildClickObserver : parentChildClickObservers) {
            parentChildClickObserver.onItemClick(view, parentPosition, childPosition);
        }
    }

}
