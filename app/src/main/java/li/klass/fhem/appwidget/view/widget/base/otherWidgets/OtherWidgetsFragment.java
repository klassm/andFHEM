package li.klass.fhem.appwidget.view.widget.base.otherWidgets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.common.base.Function;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.view.WidgetSize;
import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.fragments.core.BaseFragment;

import static android.widget.AdapterView.OnItemClickListener;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_SIZE;

public abstract class OtherWidgetsFragment extends BaseFragment {

    private WidgetSize widgetSize;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        widgetSize = (WidgetSize) args.getSerializable(APP_WIDGET_SIZE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.other_widgets_list, container, false);

        ListView listView = (ListView) view.findViewById(R.id.list);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                WidgetType widgetType = (WidgetType) view.getTag();
                OtherWidgetsFragment.this.onClick(widgetType);
            }
        });

        OtherWidgetsAdapter adapter = new OtherWidgetsAdapter(getActivity());
        listView.setAdapter(adapter);

        LinearLayout emptyView = (LinearLayout) view.findViewById(R.id.emptyView);
        fillEmptyView(emptyView, R.string.widgetNoOther);

        return view;
    }

    @Override
    public void update(boolean doUpdate) {
        checkNotNull(widgetSize);

        ListView listView = (ListView) getView().findViewById(R.id.list);
        OtherWidgetsAdapter adapter = (OtherWidgetsAdapter) listView.getAdapter();

        final List<WidgetType> widgets = WidgetType.getOtherWidgetsFor(widgetSize);
        List<Pair<WidgetType, String>> values = newArrayList(transform(widgets, new Function<WidgetType, Pair<WidgetType, String>>() {
            @Override
            public Pair<WidgetType, String> apply(WidgetType widgetType) {
                return Pair.of(widgetType, getActivity().getString(widgetType.widgetView.getWidgetName()));
            }
        }));

        if (values.isEmpty()) {
            showEmptyView();
        } else {
            hideEmptyView();
        }

        adapter.updateData(values);
    }

    protected abstract void onClick(WidgetType type);
}
