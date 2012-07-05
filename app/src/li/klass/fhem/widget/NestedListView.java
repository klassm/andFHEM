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

package li.klass.fhem.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.Set;

public class NestedListView extends ListView {

    private Drawable childDivider;

    public interface NestedListViewOnClickObserver {
        void onItemClick(View view, Object parent, Object child, int parentPosition, int childPosition);
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
        ListAdapter adapter = getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
        return (NestedListViewAdapter) adapter;
    }

    // the method overrides drawDivider in ListView
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
        Set<NestedListViewOnClickObserver> parentChildClickObservers = getNestedListViewAdapter().parentChildClickObservers;
        for (NestedListViewOnClickObserver parentChildClickObserver : parentChildClickObservers) {
            Object parent = getNestedListViewAdapter().getParents().get(parentPosition);
            Object child = getNestedListViewAdapter().getChildForParentAndChildPosition(parent, childPosition);

            parentChildClickObserver.onItemClick(view, parent, child, parentPosition, childPosition);
        }
    }

}
