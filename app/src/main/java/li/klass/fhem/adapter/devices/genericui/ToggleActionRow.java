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
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Map;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.domain.core.Device;

public abstract class ToggleActionRow<D extends Device> {

    protected abstract boolean isOn(D device);
    protected abstract void onButtonClick(final Context context, D device, boolean isChecked);

    private String description;
    private int layout;

    public static final int LAYOUT_DETAIL = R.layout.device_detail_togglebuttonrow;
    public static final int LAYOUT_OVERVIEW = R.layout.device_overview_togglebuttonrow;

    public ToggleActionRow(int description, int layout) {
        this(AndFHEMApplication.getContext().getString(description), layout);
    }

    public ToggleActionRow(String description, int layout) {
        this.description = description;
        this.layout = layout;
    }

    public TableRow createRow(Context context, LayoutInflater inflater, D device) {
        TableRow row = (TableRow) inflater.inflate(layout, null);
        assert row != null;

        ((TextView) row.findViewById(R.id.description)).setText(description);
        ToggleButton button = (ToggleButton) row.findViewById(R.id.toggleButton);
        button.setOnClickListener(createListener(context, device, button));
        setToogleButtonText(device, button);
        button.setChecked(isOn(device));

        return row;
    }
    private ToggleButton.OnClickListener createListener(final Context context, final D device, final ToggleButton button) {
        return new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(context, device, button.isChecked());
            }
        };
    }

    @SuppressWarnings("unchecked")
    protected void setToogleButtonText(D device, ToggleButton toggleButton) {
        Map<String, String> eventMap = device.getEventMap();
        if (eventMap == null) return;

        if (eventMap.containsKey("on")) {
            toggleButton.setTextOn(eventMap.get("on"));
        }

        if (eventMap.containsKey("off")) {
            toggleButton.setTextOff(eventMap.get("off"));
        }
    }
}
