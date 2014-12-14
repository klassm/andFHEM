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

package li.klass.fhem.widget;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import li.klass.fhem.R;

public class TimePickerWithSecondsDialog extends AlertDialog implements DialogInterface.OnClickListener {

    private final TimePickerWithSeconds timePicker;

    public interface TimePickerWithSecondsListener {
        void onTimeChanged(boolean okClicked, int hours, int minutes, int seconds, String formattedText);
    }

    private TimePickerWithSecondsListener listener;

    public TimePickerWithSecondsDialog(Context context, int hours, int minutes, int seconds, TimePickerWithSecondsListener listener) {
        super(context);

        this.listener = listener;
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.timepicker_with_seconds_dialog, null);

        timePicker = (TimePickerWithSeconds) view.findViewById(R.id.timePickerWithSeconds);
        timePicker.setHours(hours);
        timePicker.setMinutes(minutes);
        timePicker.setSeconds(seconds);

        setButton(BUTTON_POSITIVE, context.getString(R.string.okButton), this);
        setButton(BUTTON_NEGATIVE, context.getString(R.string.cancelButton), this);

        setView(view);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (listener == null) return;

        listener.onTimeChanged(which == BUTTON_POSITIVE, timePicker.getHours(), timePicker.getMinutes(),
                timePicker.getSeconds(), timePicker.getFormattedValue());
    }
}
