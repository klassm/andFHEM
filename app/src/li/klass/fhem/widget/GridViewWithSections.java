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

package li.klass.fhem.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;

import java.util.HashSet;
import java.util.Set;

public class GridViewWithSections extends GridView {

    public interface GridViewWithSectionsOnClickObserver {
        void onItemClick(View view, Object parent, Object child, int parentPosition, int childPosition);
    }

    public GridViewWithSections(Context context) {
        super(context);
    }

    public GridViewWithSections(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridViewWithSections(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public GridViewWithSectionsAdapter getGridViewWithSectionsAdapter() {
        ListAdapter adapter = getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
        return (GridViewWithSectionsAdapter) adapter;
    }


    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!(adapter instanceof GridViewWithSectionsAdapter)) {
            throw new RuntimeException("I am expecting a " + GridViewWithSectionsAdapter.class.getSimpleName());
        }
        super.setAdapter(adapter);
        setNumColumns(((GridViewWithSectionsAdapter) adapter).getNumberOfColumns());
        setVerticalSpacing(10);
    }

    @Override
    public boolean performItemClick(View view, int position, long id) {
        GridViewWithSectionsAdapter adapter = getGridViewWithSectionsAdapter();

        int parentPosition = adapter.findOriginalParentPosition(position);
        int childPosition = position - adapter.findParentPositionForChildPosition(position) - adapter.getNumberOfColumns();

        performParentChildItemClick(view, parentPosition, childPosition);
        return super.performItemClick(view, parentPosition, id);
    }

    @SuppressWarnings("unchecked")
    private void performParentChildItemClick(View view, int parentPosition, int childPosition) {
        Set<GridViewWithSectionsOnClickObserver> parentChildClickObservers = getGridViewWithSectionsAdapter().getClickObservers();
        for (GridViewWithSectionsOnClickObserver parentChildClickObserver : parentChildClickObservers) {
            Object parent = getGridViewWithSectionsAdapter().getParents().get(parentPosition);
            Object child = getGridViewWithSectionsAdapter().getChildForParentAndChildPosition(parent, childPosition);

            parentChildClickObserver.onItemClick(view, parent, child, parentPosition, childPosition);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            updateNumberOfColumns();
        }

        super.onLayout(changed, l, t, r, b);
    }

    public void updateNumberOfColumns() {
        Log.i(GridViewWithSections.class.getName(), "update number of columns");
        GridViewWithSectionsAdapter adapter = getGridViewWithSectionsAdapter();
        if (adapter == null) {
            return;
        }
        int horizontalSpacing = 20;

        // code equivalent to the one used for auto calculating the width in Android's GridView
        adapter.setNumberOfColumns((getMeasuredWidth() + horizontalSpacing) / (adapter.getRequiredColumnWidth() + horizontalSpacing));

        setHorizontalSpacing(horizontalSpacing);
        setNumColumns(AUTO_FIT);
        setColumnWidth(adapter.getRequiredColumnWidth());
    }
}

