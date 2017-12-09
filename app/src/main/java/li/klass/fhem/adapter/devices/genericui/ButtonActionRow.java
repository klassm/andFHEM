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
import android.view.*;
import android.widget.*;
import li.klass.fhem.R;

public abstract class ButtonActionRow {
    public static final int LAYOUT_DETAIL = R.layout.device_detail_buttonrow;
    public static final int LAYOUT_OVERVIEW = R.layout.device_overview_buttonrow;
    private final String description;
    private String buttonText;
    private int layout;

    public ButtonActionRow(Context context, int buttonText, int layout) {
        this(context.getString(buttonText), layout);
    }

    public ButtonActionRow(String description, String buttonText, int layout) {
        this.description = description;
        this.buttonText = buttonText;
        this.layout = layout;
    }

    public ButtonActionRow(String buttonText, int layout) {
        this("", buttonText, layout);
    }

    @SuppressWarnings("unchecked")
    public TableRow createRow(LayoutInflater inflater) {
        TableRow row = (TableRow) inflater.inflate(layout, null);
        ((TextView) row.findViewById(R.id.description)).setText(description);

        Button button = (Button) row.findViewById(R.id.button);
        button.setText(buttonText);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick();
            }
        });

        return row;
    }

    protected abstract void onButtonClick();
}
