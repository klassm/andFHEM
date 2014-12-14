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

package li.klass.fhem.adapter.devices.genericui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.ensequence.socialmediatestharness.ui.FlowLayout;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.domain.core.Device;

public abstract class HolderActionRow<D extends Device<D>, I> {
    private String description;
    private int layout;

    public static final int LAYOUT_DETAIL = R.layout.device_detail_holder_row;
    public static final int LAYOUT_OVERVIEW = R.layout.device_overview_holder_row;

    public HolderActionRow(String description, int layout) {
        this.description = description;
        this.layout = layout;
    }

    public TableRow createRow(final Context context, LayoutInflater inflater, ViewGroup viewGroup, final D device) {
        TableRow row = (TableRow) inflater.inflate(layout, null);
        viewGroup.addView(row);

        FlowLayout holder = (FlowLayout) row.findViewById(R.id.holder);

        TextView descriptionView = (TextView) row.findViewById(R.id.description);
        if (descriptionView != null) {
            descriptionView.setText(description);
        }

        for (I item : getItems(device)) {
            View view = viewFor(item, device, inflater, context, viewGroup);
            if (view != null) {
                holder.addView(view);
            }
        }

        return row;
    }

    public abstract List<I> getItems(D device);
    public abstract View viewFor(I item, D device, LayoutInflater inflater, Context context, ViewGroup viewGroup);
}
