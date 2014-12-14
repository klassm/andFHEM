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

package li.klass.fhem.widget.deviceFunctionality;

import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ericharlow.DragNDrop.DragNDropAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import li.klass.fhem.R;

import static com.google.common.base.Preconditions.checkNotNull;

public class DeviceFunctionalityOrderAdapter extends DragNDropAdapter<DeviceFunctionalityPreferenceWrapper> {

    private OrderActionListener listener;

    private static final Logger LOG = LoggerFactory.getLogger(DeviceFunctionalityOrderAdapter.class);

    public DeviceFunctionalityOrderAdapter(Context context, int resource,
                                           ArrayList<DeviceFunctionalityPreferenceWrapper> data) {
        super(context, resource, data);
    }

    public void setListener(OrderActionListener orderActionListener) {
        listener = orderActionListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DeviceFunctionalityPreferenceWrapper item = (DeviceFunctionalityPreferenceWrapper) getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(resource, null);
        }
        assert convertView != null;

        setOnClickAction(OrderAction.VISIBILITY_CHANGE, item, convertView);

        updateContent(item, convertView);

        return convertView;
    }

    private void setOnClickAction(final OrderAction action,
                                  final DeviceFunctionalityPreferenceWrapper item, final View convertView) {
        ImageButton button = (ImageButton) convertView.findViewById(R.id.change_visibility);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkNotNull(view);
                listener.deviceTypeReordered(item, action);
                notifyDataSetChanged();
            }
        });
    }

    private void updateContent(DeviceFunctionalityPreferenceWrapper item, View view) {
        TextView nameView = (TextView) view.findViewById(R.id.name);
        nameView.setText(item.getDeviceFunctionality().getCaptionText(context));

        ImageButton visibilityButton = (ImageButton) view.findViewById(R.id.change_visibility);

        LOG.debug("updateContent() - drawing content for {}, visiblity is {}", item.getDeviceFunctionality().name(), item.isVisible());

        if (!item.isVisible()) {
            nameView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
            nameView.setTextColor(context.getResources().getColor(R.color.textColorSecondary));
            visibilityButton.setImageResource(R.drawable.invisible);
        } else {
            nameView.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);
            nameView.setTextColor(context.getResources().getColor(R.color.textColorDefault));
            visibilityButton.setImageResource(R.drawable.visible);
        }
    }

    @Override
    protected boolean doSort() {
        return false;
    }

    enum OrderAction {
        UP, DOWN, VISIBILITY_CHANGE
    }

    interface OrderActionListener {
        void deviceTypeReordered(DeviceFunctionalityPreferenceWrapper wrapper, OrderAction action);
    }
}
