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

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import li.klass.fhem.R;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.setlist.typeEntry.RGBSetListEntry;
import li.klass.fhem.room.list.backend.xmllist.XmlListDevice;
import li.klass.fhem.util.ColorUtil;

public class StateChangingColorPickerRow {
    private final XmlListDevice xmlListDevice;
    private final StateUiService stateUiService;
    private final RGBSetListEntry rgbSetListEntry;
    private final String connectionId;

    public StateChangingColorPickerRow(StateUiService stateUiService, XmlListDevice xmlListDevice, String connectionId, RGBSetListEntry rgbSetListEntry) {
        this.xmlListDevice = xmlListDevice;
        this.stateUiService = stateUiService;
        this.rgbSetListEntry = rgbSetListEntry;
        this.connectionId = connectionId;
    }

    public TableRow createRow(final Context context, final LayoutInflater inflater, ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.device_detail_colorpicker_row, viewGroup, false);
        final String rgb = xmlListDevice.getState(rgbSetListEntry.getKey()).get();
        assert view != null;

        final View colorValueView = view.findViewById(R.id.color_value);
        colorValueView.setBackgroundColor(ColorUtil.fromRgbString(rgb) | 0xFF000000);

        Button setButton = (Button) view.findViewById(R.id.set);

        TextView description = (TextView) view.findViewById(R.id.description);
        description.setText("");


        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new RGBColorPickerDialog(context, rgb, new RGBColorPickerDialog.Callback() {
                    @Override
                    public void onColorChanged(String newRGB, Dialog dialog) {
                        stateUiService.setSubState(xmlListDevice, rgbSetListEntry.getKey(), newRGB, connectionId, context);
                    }

                    @Override
                    public void onColorUnchanged(Dialog dialog) {
                    }
                }).show();
            }
        });

        return (TableRow) view;
    }
}
