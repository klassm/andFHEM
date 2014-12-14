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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;

public class GridViewWithSections extends GridView {

    public interface OnClickListener<PARENT, CHILD> {
        boolean onItemClick(View view, PARENT parent, CHILD child, int parentPosition, int childPosition);
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

    public void setOnLongClickListener(final OnClickListener listener) {
        setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return performParentChildItemClick(view, position, listener);
            }
        });
    }

    public void setOnClickListener(final OnClickListener listener) {
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                performParentChildItemClick(view, position, listener);
            }
        });
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

    @SuppressWarnings("unchecked")
    private boolean performParentChildItemClick(View view, int position, OnClickListener listener) {
        GridViewWithSectionsAdapter adapter = getGridViewWithSectionsAdapter();

        int parentPosition = adapter.findOriginalParentPosition(position);
        int childPosition = position - adapter.findParentPositionForChildPosition(position) - adapter.getNumberOfColumns();

        Object parent = getGridViewWithSectionsAdapter().getDeviceGroupParents().get(parentPosition);
        Object child = getGridViewWithSectionsAdapter().getChildForParentAndChildPosition(parent, childPosition);

        return listener.onItemClick(view, parent, child, parentPosition, childPosition);
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

