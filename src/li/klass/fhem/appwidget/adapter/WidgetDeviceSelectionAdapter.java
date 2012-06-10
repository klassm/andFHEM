/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.appwidget.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import li.klass.fhem.adapter.rooms.RoomDetailAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.RoomDeviceList;

public class WidgetDeviceSelectionAdapter extends RoomDetailAdapter {

    public WidgetDeviceSelectionAdapter(Context context, RoomDeviceList roomDeviceList) {
        super(context, roomDeviceList);

    }

    @Override
    protected View getChildView(Device<?> child, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
        TextView content = (TextView) view.findViewById(android.R.id.text1);
        content.setText(child.getAliasOrName());

        return view;
    }
}
