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

package li.klass.fhem.adapter.devices.genericui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.domain.core.Device;

public abstract class UpDownButtonRow<T extends Device> {
    private String description;

    public static final int LAYOUT_DETAIL = R.layout.device_detail_updownbuttonrow;


    public UpDownButtonRow(String description) {
        this.description = description;
    }

    public TableRow createRow(final Context context, LayoutInflater inflater, final T device) {
        TableRow row = (TableRow) inflater.inflate(LAYOUT_DETAIL, null);
        ((TextView) row.findViewById(R.id.description)).setText(description);

        Button downButton = (Button) row.findViewById(R.id.dimDown);
        downButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                onDownButtonClick(context, device);
            }
        });

        Button upButton = (Button) row.findViewById(R.id.dimUp);
        upButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                onUpButtonClick(context, device);
            }
        });
        return row;
    }

    public abstract void onUpButtonClick(Context context, T device);
    public abstract void onDownButtonClick(Context context, T device);
}
