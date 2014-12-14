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

package li.klass.fhem.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListDataAdapter<T extends Comparable<T>> extends BaseAdapter {

    protected volatile List<T> data;
    protected LayoutInflater inflater;
    protected int resource;
    protected Context context;
    private Comparator<T> comparator = null;

    public ListDataAdapter(Context context, int resource, List<T> data) {
        this(context, resource, data, null);
    }

    public ListDataAdapter(Context context, int resource, List<T> data, Comparator<T> comparator) {
        this.comparator = comparator;
        this.data = data;
        this.inflater = LayoutInflater.from(context);
        this.resource = resource;
        this.context = context;
        sortData();
    }

    public void updateData(List<T> newData) {
        // do this ONLY if the references do NOT match!!
        if (data != newData) {
            data.clear();
            data.addAll(newData);
        }
        sortData();

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (data == null) return 0;
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        TextView text;

        if (convertView == null) {
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        try {
            text = (TextView) view;
        } catch (ClassCastException e) {
            String errorText = "Assumed the whole view is a TextView. Derive to change that.";
            Log.e(ListDataAdapter.class.getName(), errorText);
            throw new IllegalStateException(errorText);
        }

        T item = (T) getItem(position);
        text.setText(item.toString());

        return view;
    }

    private void sortData() {
        if (! doSort()) return;

        if (data == null || data.size() == 0) return;
        if (comparator != null) {
            Collections.sort(data, comparator);
        } else {
            Collections.sort(data);
        }
    }


    @Override
    public boolean isEmpty() {
        return data == null || data.size() == 0;
    }

    public List<T> getData() {
        return Collections.unmodifiableList(data);
    }

    protected boolean doSort() {
        return true;
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
