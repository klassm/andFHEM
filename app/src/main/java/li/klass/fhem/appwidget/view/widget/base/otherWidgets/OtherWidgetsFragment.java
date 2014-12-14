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

import java.io.Serializable;
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
import static li.klass.fhem.constants.BundleExtraKeys.ON_CLICKED_CALLBACK;

public class OtherWidgetsFragment extends BaseFragment {

    private WidgetSize widgetSize;
    private OnWidgetClickedCallback widgetClickedCallback;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        widgetSize = (WidgetSize) args.getSerializable(APP_WIDGET_SIZE);
        widgetClickedCallback = (OnWidgetClickedCallback) args.getSerializable(ON_CLICKED_CALLBACK);
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
        fillEmptyView(emptyView, R.string.widgetNoOther, container);

        return view;
    }

    protected void onClick(WidgetType type) {
        if (widgetClickedCallback != null) {
            widgetClickedCallback.onWidgetClicked(type);
        }
    }

    @Override
    public void update(boolean doUpdate) {
        checkNotNull(widgetSize);

        if (getView() == null) return;

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

    public interface OnWidgetClickedCallback extends Serializable {
        void onWidgetClicked(WidgetType widgetType);
    }
}
