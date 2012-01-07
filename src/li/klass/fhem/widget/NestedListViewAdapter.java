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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import java.util.*;

public abstract class NestedListViewAdapter<P, C> extends BaseAdapter implements ListAdapter {

    public Set<NestedListView.NestedListViewOnClickObserver> parentChildClickObservers = new HashSet<NestedListView.NestedListViewOnClickObserver>();

    private Map<Integer, P> parentPositions = new HashMap<Integer, P>();
    private int totalItems = 0;

    protected LayoutInflater layoutInflater;

    public NestedListViewAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    public void updateParentPositions() {
        parentPositions = new HashMap<Integer, P>();

        int currentPosition = 0;
        for (P item : getParents()) {
            parentPositions.put(currentPosition, item);
            currentPosition += getChildrenCountForParent(item);
            currentPosition++;
        }
        totalItems = currentPosition;
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

                return getChildView(child, view, viewGroup);
            }
        }catch (Exception e) {
            Log.e(NestedListViewAdapter.class.getName(), "error occurred", e);
            return null;
        }
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

    public boolean isParent(int position) {
        return parentPositions.containsKey(position);
    }

    public long getItemId(int i) {
        return i;
    }

    protected void updateData() {
        updateParentPositions();
        notifyDataSetChanged();
    }


    public void addParentChildObserver(NestedListView.NestedListViewOnClickObserver observer) {
        parentChildClickObservers.add(observer);
    }

    public void removeParentChildObserver(NestedListView.NestedListViewOnClickObserver observer) {
        parentChildClickObservers.remove(observer);
    }

    protected abstract C getChildForParentAndChildPosition(P parent, int childPosition);
    protected abstract int getChildrenCountForParent(P parent);

    protected abstract View getParentView(P parent, View view, ViewGroup viewGroup);
    protected abstract View getChildView(C child, View view, ViewGroup viewGroup);

    protected abstract List<P> getParents();
}
