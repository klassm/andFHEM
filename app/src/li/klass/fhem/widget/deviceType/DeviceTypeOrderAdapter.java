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

package li.klass.fhem.widget.deviceType;

import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.ericharlow.DragNDrop.DragNDropAdapter;
import li.klass.fhem.R;
import li.klass.fhem.util.Reject;

import java.util.ArrayList;

public class DeviceTypeOrderAdapter extends DragNDropAdapter<DeviceTypePreferenceWrapper> {

    enum DeviceTypeOrderAction {
        UP, DOWN, VISIBILITY_CHANGE
    }

    interface DeviceTypeOrderActionListener {

        void deviceTypeReordered(DeviceTypePreferenceWrapper wrapper, DeviceTypeOrderAction action);
    }
    private DeviceTypeOrderActionListener listener;

    public DeviceTypeOrderAdapter(Context context, int resource, ArrayList<DeviceTypePreferenceWrapper> data) {
        super(context, resource, data);
    }

    public void setListener(DeviceTypeOrderActionListener orderActionListener) {
        listener = orderActionListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DeviceTypePreferenceWrapper item = (DeviceTypePreferenceWrapper) getItem(position);

        View view = inflater.inflate(resource, null);
        TextView nameView = (TextView) view.findViewById(R.id.name);
        nameView.setText(item.getDeviceType().name());

        ImageButton downButton = (ImageButton) view.findViewById(R.id.move_down);
        ImageButton upButton = (ImageButton) view.findViewById(R.id.move_up);
        ImageButton visibilityButton = (ImageButton) view.findViewById(R.id.change_visibility);

        setOnClickAction(downButton, DeviceTypeOrderAction.DOWN, item);
        setOnClickAction(upButton, DeviceTypeOrderAction.UP, item);
        setOnClickAction(visibilityButton, DeviceTypeOrderAction.VISIBILITY_CHANGE, item);

        if (! item.isVisible()) {
            nameView.setPaintFlags(nameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            nameView.setPaintFlags(nameView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        }
        return view;
    }

    private void setOnClickAction(ImageButton button, final DeviceTypeOrderAction action, final DeviceTypePreferenceWrapper item) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Reject.ifNull(listener);
                listener.deviceTypeReordered(item, action);
            }
        });
    }

    @Override
    protected boolean doSort() {
        return false;
    }

}
