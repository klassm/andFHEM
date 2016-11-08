package li.klass.fhem.appwidget.view.widget.base.otherWidgets;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.tuple.Pair;

import li.klass.fhem.R;
import li.klass.fhem.adapter.ListDataAdapter;
import li.klass.fhem.appwidget.view.WidgetType;

public class OtherWidgetsAdapter extends ListDataAdapter<Pair<WidgetType, String>> {
    public OtherWidgetsAdapter(Context context) {
        super(context, R.layout.simple_list_item, Lists.newArrayList(),
                (pair1, pair2) -> pair1.getValue().compareTo(pair2.getValue()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getView(int position, View view, ViewGroup parent) {
        Pair<WidgetType, String> item = (Pair<WidgetType, String>) getItem(position);

        if (view == null) {
            view = inflater.inflate(resource, null);
        }

        TextView textView = (TextView) view.findViewById(R.id.text);
        textView.setText(item.getValue());

        view.setTag(item.getKey());

        return view;
    }
}
