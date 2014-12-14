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
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class GridViewWithSectionsAdapter<P, C> extends BaseAdapter {

    public static final String TAG = GridViewWithSectionsAdapter.class.getName();

    private Map<Integer, P> parentPositions;
    private int totalNumberOfItems;
    protected final Context context;
    protected final LayoutInflater layoutInflater;

    private int currentRowIndex;
    private int currentRowParentIndex;
    private int currentRowHeight;
    private List<View> currentRowViews = new ArrayList<View>();
    private int numberOfColumns = -1;

    public GridViewWithSectionsAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    public void updateParentPositions() {
        List<P> parents = getDeviceGroupParents();
        Log.v(TAG, "updating parent positions for parent count " + parents.size());

        parentPositions = Maps.newHashMap();

        int numberOfColumns = getNumberOfColumns();
        int currentPosition = 0;
        for (P parent : parents) {
            parentPositions.put(currentPosition, parent);

            // add all the children plus an offset to complete the grid row
            int childCount = getChildrenCountForParent(parent);

            int filledItemsWithinTheRow = childCount % numberOfColumns;
            int childOffset = filledItemsWithinTheRow == 0 ? 0 : numberOfColumns - filledItemsWithinTheRow;
            currentPosition += childCount + childOffset;

            // add the parent row
            currentPosition += numberOfColumns;
        }
        totalNumberOfItems = currentPosition;
        Log.v(TAG, "found " + totalNumberOfItems + " items");
    }

    @Override
    public int getCount() {
        Log.v(TAG, "returning totalNumberOfItems: " + totalNumberOfItems);
        return totalNumberOfItems;
    }

    @Override
    public Object getItem(int position) {
        P parent = getParentForPosition(position);
        if (parent != null) {
            return parent;
        }

        int parentPosition = findParentPositionForChildPosition(position);
        parent = parentPositions.get(parentPosition);

        int relativeChildPosition = position - parentPosition;
        return getChildForParentAndChildPosition(parent, relativeChildPosition);
    }

    public int findParentPositionForChildPosition(int flatPosition) {
        Set<Integer> keyPositions = parentPositions.keySet();

        int bestKeyMatch = 0;
        int bestKeyDiff = -1;

        for (int keyPosition : keyPositions) {
            int diff = flatPosition - keyPosition;
            if (diff >= 0 && (bestKeyDiff == -1 || diff < bestKeyDiff)) {
                bestKeyDiff = diff;
                bestKeyMatch = keyPosition;
            }
        }
        return bestKeyMatch;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int flatPosition, View view, ViewGroup viewGroup) {
        Log.v(TAG, "drawing flatPosition " + flatPosition + "/" + totalNumberOfItems);
        try {
            int parentBasePosition = getParentBasePosition(flatPosition);
            if (parentBasePosition != -1) {
                P parent = parentPositions.get(parentBasePosition);
                int parentOffset = flatPosition - parentBasePosition;
                return getParentView(parent, parentOffset, view, viewGroup);
            } else {
                int parentPosition = findParentPositionForChildPosition(flatPosition);
                P parent = parentPositions.get(parentPosition);

                int relativeChildPosition = flatPosition - parentPosition - getNumberOfColumns();
                C child = getChildForParentAndChildPosition(parent, relativeChildPosition);

                View childView = getChildView(parent, parentPosition, child, view, viewGroup);
                updateChildrenRowHeight(getNumberOfColumns(), parentPosition, relativeChildPosition, childView);

                return childView;
            }
        } catch (Exception e) {
            Log.e(TAG, "error occurred", e);
            return null;
        }
    }

    private void updateChildrenRowHeight(int columns, int parentIndex, int childOffset, View childView) {
        int rowIndex = getRowForChildOffset(columns, childOffset);
        if (currentRowParentIndex != parentIndex || currentRowIndex != rowIndex) {
            currentRowViews.clear();
            currentRowHeight = 0;
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        childView.measure(widthMeasureSpec, heightMeasureSpec);

        int measuredHeight = childView.getMeasuredHeight();
        currentRowViews.add(childView);
        if (measuredHeight > currentRowHeight) {
            currentRowHeight = measuredHeight;
        }

        setHeightForViews(currentRowHeight, currentRowViews);

        currentRowIndex = rowIndex;
        currentRowParentIndex = parentIndex;
    }

    private int getRowForChildOffset(int columns, int childOffset) {
        return childOffset / columns;
    }

    private void setHeightForViews(int height, List<View> views) {
        for (View view : views) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            layoutParams.height = height;
            view.setLayoutParams(layoutParams);
        }
    }

    protected P getParentForPosition(int position) {
        int basePosition = getParentBasePosition(position);
        if (basePosition == -1) return null;

        return parentPositions.get(basePosition);
    }

    protected int getParentBasePosition(int position) {
        int numberOfColumns = getNumberOfColumns();
        for (Integer key : parentPositions.keySet()) {
            if (key <= position && key + numberOfColumns > position) {
                return key;
            }
        }
        return -1;
    }

    protected void updateData() {
        updateParentPositions();
        notifyDataSetChanged();
    }

    public int findOriginalParentPosition(int flatPosition) {
        P parent = getParentForPosition(flatPosition);
        if (parent != null) {
            parent = parentPositions.get(flatPosition);
        } else {
            parent = parentPositions.get(findParentPositionForChildPosition(flatPosition));
        }

        List<P> parents = getDeviceGroupParents();
        for (int i = 0; i < parents.size(); i++) {
            if (parents.get(i).equals(parent)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return totalNumberOfItems == 0;
    }

    protected int getNumberOfColumns() {
        if (numberOfColumns <= 0) return 1;
        return numberOfColumns;
    }

    public void setNumberOfColumns(int numberOfColumns) {
        this.numberOfColumns = numberOfColumns;
        Log.d(TAG, "set grid view to " + numberOfColumns + " columns");
        updateParentPositions();
        notifyDataSetChanged();
    }

    protected int getFlatPositionForParentAndChild(P parent, C child) {
        int parentPosition = getParentPositionFor(parent);
        if (parentPosition == -1) return -1;

        int count = getChildrenCountForParent(parent);
        for (int i = 0; i < count; i++) {
            C aChild = getChildForParentAndChildPosition(parent, i);
            if (aChild.equals(child)) {
                return parentPosition + i;
            }
        }
        return -1;
    }

    protected int getParentPositionFor(P parent) {
        for (Integer position : parentPositions.keySet()) {
            if (parentPositions.get(position).equals(parent)) {
                return position;
            }
        }
        return -1;
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        // Workaround for a silly bug in Android 4
        // see http://code.google.com/p/android/issues/detail?id=22946 for details
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }

    protected abstract C getChildForParentAndChildPosition(P parent, int childPosition);

    protected abstract int getChildrenCountForParent(P parent);

    protected abstract View getParentView(P parent, int parentOffset, View view, ViewGroup viewGroup);

    protected abstract View getChildView(P parent, int parentPosition, C child, View view, ViewGroup viewGroup);

    protected abstract List<P> getDeviceGroupParents();

    protected abstract int getRequiredColumnWidth();
}