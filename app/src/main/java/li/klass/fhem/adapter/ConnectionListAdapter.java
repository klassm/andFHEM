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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.fhem.connection.FHEMServerSpec;

public class ConnectionListAdapter extends ListDataAdapter<FHEMServerSpec> {
    private String selectedConnectionId;

    public ConnectionListAdapter(Context context, List<FHEMServerSpec> data) {
        super(context, R.layout.connection_list_entry, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FHEMServerSpec server = (FHEMServerSpec) getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(resource, null);
        }

        assert convertView != null;

        TextView nameView = (TextView) convertView.findViewById(R.id.name);
        nameView.setText(server.getName());

        TextView typeView = (TextView) convertView.findViewById(R.id.type);
        if (server.getServerType() != null) {
            typeView.setText(server.getServerType().name());
        }

        convertView.setTag(server.getId());

        if (server.getId().equals(selectedConnectionId)) {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.android_green));
        }

        return convertView;
    }

    public void updateData(List<FHEMServerSpec> newData, String selectedConnectionId) {
        if (newData == null) return;

        this.selectedConnectionId = selectedConnectionId;
        updateData(newData);
    }
}
