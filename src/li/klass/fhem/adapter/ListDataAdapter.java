package li.klass.fhem.adapter;

import android.content.Context;
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
        data.clear();
        data.addAll(newData);
        sortData();

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
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
        if (comparator != null) {
            Collections.sort(data, comparator);
        } else {
            Collections.sort(data);
        }
    }
}
