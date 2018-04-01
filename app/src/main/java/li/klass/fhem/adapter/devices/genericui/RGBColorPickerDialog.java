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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.chiralcode.colorpicker.ColorPicker;
import com.chiralcode.colorpicker.ColorPickerListener;

import li.klass.fhem.R;
import li.klass.fhem.util.ColorUtil;

public class RGBColorPickerDialog extends AlertDialog implements DialogInterface.OnClickListener {

    private final Callback callback;
    private final int initialColor;
    private int newColor;

    public RGBColorPickerDialog(Context context, String initialRGB, Callback callback) {
        super(context);

        this.initialColor = ColorUtil.INSTANCE.fromRgbString(initialRGB) | 0xFF000000;
        this.newColor = initialColor;
        this.callback = callback;

        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.colorpicker_dialog, null);


        final CheckBox sendEachChangeCheckbox = view.findViewById(R.id.sendEachChange);

        final ColorPicker picker = view.findViewById(R.id.colorPicker);
        picker.setColor(initialColor);
        picker.setListener(new ColorPickerListener() {
            @Override
            public void onColorChange(int color) {
                newColor = color & 0x00FFFFFF;
                if (!sendEachChangeCheckbox.isChecked()) return;

                // remove alpha channel first!
                RGBColorPickerDialog.this.onColorChange(newColor);
            }
        });

        setButton(BUTTON_POSITIVE, context.getString(R.string.okButton), this);
        setButton(BUTTON_NEGATIVE, context.getString(R.string.cancelButton), this);

        setView(view);
    }

    private void onColorChange(int color) {
        if (newColor != initialColor) {
            callback.onColorChanged(ColorUtil.INSTANCE.toHexStringWithoutPrefix(color), RGBColorPickerDialog.this);
        } else {
            callback.onColorUnchanged(RGBColorPickerDialog.this);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        onColorChange(newColor);
    }

    public interface Callback {
        void onColorChanged(String newRGB, Dialog dialog);

        void onColorUnchanged(Dialog dialog);
    }
}
