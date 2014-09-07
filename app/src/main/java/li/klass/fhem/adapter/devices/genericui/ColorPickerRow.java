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

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableRow;
import android.widget.TextView;

import com.chiralcode.colorpicker.ColorPicker;
import com.chiralcode.colorpicker.ColorPickerListener;

import li.klass.fhem.R;
import li.klass.fhem.util.DialogUtil;

public class ColorPickerRow implements ColorPickerListener {
    private final int originalValue;
    private int value;
    private int alertDialogTitle;

    public ColorPickerRow(int value, int alertDialogTitle) {
        this.originalValue = value;
        this.value = value;
        this.alertDialogTitle = alertDialogTitle;
    }

    public TableRow createRow(final Context context, final LayoutInflater inflater, ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.device_detail_colorpicker_row, viewGroup, false);
        assert view != null;

        value |= 0xFF000000;
        final View colorValueView = view.findViewById(R.id.color_value);
        colorValueView.setBackgroundColor(value);

        Button setButton = (Button) view.findViewById(R.id.set);

        TextView description = (TextView) view.findViewById(R.id.description);
        description.setText("");



        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @SuppressLint("InflateParams") final View contentView = inflater.inflate(R.layout.colorpicker_dialog, null);
                assert contentView != null;

                final CheckBox sendEachChangeCheckbox =
                        (CheckBox) contentView.findViewById(R.id.sendEachChange);

                final ColorPicker picker = (ColorPicker) contentView.findViewById(R.id.colorPicker);
                picker.setColor(value);
                picker.setListener(new ColorPickerListener() {
                    @Override
                    public void onColorChange(int color) {
                        if (! sendEachChangeCheckbox.isChecked()) return;

                        // remove alpha channel first!
                        ColorPickerRow.this.onColorChange(color & 0x00FFFFFF);
                    }
                });

                String title = context.getString(alertDialogTitle);
                DialogUtil.showContentDialog(context, title, contentView, new DialogUtil.AlertOnClickListener() {
                    @Override
                    public void onClick() {
                        value = picker.getColor();
                        colorValueView.setBackgroundColor(value);

                        if (originalValue != value) {
                            onColorChange(value & 0x00FFFFFF);
                        }
                    }
                });
            }
        });

        return (TableRow) view;
    }

    @Override
    public void onColorChange(int color) {
    }
}
