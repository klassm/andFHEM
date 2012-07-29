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

package li.klass.fhem.adapter.devices.genericui;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import li.klass.fhem.R;

public abstract class CustomViewTableRow {

    public TableRow createRow(LayoutInflater inflater) {
        TableRow tableRow = (TableRow) inflater.inflate(R.layout.device_detail_generic_view_row, null);
        RelativeLayout layout = (RelativeLayout) tableRow.findViewById(R.id.value);

        View contentView = getContentView();
        layout.addView(contentView);

        return tableRow;
    }

    public abstract View getContentView();
}
