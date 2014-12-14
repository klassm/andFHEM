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
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;

public abstract class NestedListViewAdapter<P, C> extends BaseAdapter implements ListAdapter {

    protected Set<NestedListView.NestedListViewOnClickObserver> parentChildClickObservers = new HashSet<NestedListView.NestedListViewOnClickObserver>();
    protected LayoutInflater layoutInflater;
    private Map<Integer, P> parentPositions = newHashMap();
    private int totalItems = 0;

    public NestedListViewAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int flatPosition, View view, ViewGroup viewGroup) {
        try {
            if (isParent(flatPosition)) {
                P parent = parentPositions.get(flatPosition);
                return getParentView(parent, view, viewGroup);
            } else {
                int parentPosition = findParentPositionForChildPosition(flatPosition);
                P parent = parentPositions.get(parentPosition);

                int relativeChildPosition = flatPosition - parentPosition - 1;
                C child = getChildForParentAndChildPosition(parent, relativeChildPosition);

                return getChildView(parent, parentPosition, child, view, viewGroup, relativeChildPosition);
            }
        } catch (Exception e) {
            Log.e(NestedListViewAdapter.class.getName(), "error occurred", e);
            return null;
        }
    }

    public boolean isParent(int position) {
        return parentPositions.containsKey(position);
    }

    protected abstract View getParentView(P parent, View view, ViewGroup viewGroup);

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

    protected abstract C getChildForParentAndChildPosition(P parent, int childPosition);

    protected abstract View getChildView(P parent, int parentPosition, C child, View view, ViewGroup viewGroup, int relativeChildPosition);

    public int findOriginalParentPosition(int flatPosition) {
        P parent;
        if (isParent(flatPosition)) {
            parent = parentPositions.get(flatPosition);
        } else {
            parent = parentPositions.get(findParentPositionForChildPosition(flatPosition));
        }

        List<P> parents = getParents();
        for (int i = 0; i < parents.size(); i++) {
            if (parents.get(i).equals(parent)) {
                return i;
            }
        }
        return -1;
    }

    protected abstract List<P> getParents();

    public int getCount() {
        return totalItems;
    }

    public Object getItem(int position) {
        if (isParent(position)) {
            return parentPositions.get(position);
        } else {
            int parentPosition = findParentPositionForChildPosition(position);
            P parent = parentPositions.get(parentPosition);

            int relativeChildPosition = position - parentPosition;
            return getChildForParentAndChildPosition(parent, relativeChildPosition);
        }
    }

    public long getItemId(int i) {
        return i;
    }

    protected void updateData() {
        updateParentPositions();
        notifyDataSetChanged();
    }

    public void updateParentPositions() {
        parentPositions = newHashMap();

        int currentPosition = 0;
        for (P item : getParents()) {
            parentPositions.put(currentPosition, item);
            currentPosition += getChildrenCountForParent(item);
            currentPosition++;
        }
        totalItems = currentPosition;
    }

    protected abstract int getChildrenCountForParent(P parent);

    public void addParentChildObserver(NestedListView.NestedListViewOnClickObserver observer) {
        parentChildClickObservers.add(observer);
    }

    public void removeParentChildObserver(NestedListView.NestedListViewOnClickObserver observer) {
        parentChildClickObservers.remove(observer);
    }

    @Override
    public boolean isEmpty() {
        return totalItems == 0;
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        // Workaround for a silly bug in Android 4
        // see http://code.google.com/p/android/issues/detail?id=22946 for details
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }
}
