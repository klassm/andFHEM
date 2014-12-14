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

package li.klass.fhem.adapter.weekprofile;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.domain.heating.schedule.DayProfile;
import li.klass.fhem.domain.heating.schedule.interval.FromToHeatingInterval;

public class FromToWeekProfileAdapter
        extends BaseWeekProfileAdapter<FromToHeatingInterval> {


    protected interface OnTimeChangedListener {
        void onTimeChanged(String newTime);
    }

    public FromToWeekProfileAdapter(Context context) {
        super(context);
    }

    @Override
    protected int getNumberOfAdditionalChildrenForParent() {
        return 0;
    }

    @Override
    protected View getChildView(DayProfile<FromToHeatingInterval, ?, ?> parent, int parentPosition,
                                final FromToHeatingInterval child, View v, ViewGroup viewGroup, int relativeChildPosition) {
        final View view = layoutInflater.inflate(R.layout.weekprofile_from_to_item, viewGroup, false);

        setDetailTextView(view, R.id.from, child.getChangedFromTime(), child.getFromTime(), false);
        setDetailTextView(view, R.id.to, child.getChangedToTime(), child.getToTime(), false);

        setChangeTimeButton(view, R.id.fromSet, child.getChangedFromTime(), new OnTimeChangedListener() {
            @Override
            public void onTimeChanged(String newTime) {
                child.setChangedFromTime(newTime);
                notifyWeekProfileChangedListener();
            }
        });

        setChangeTimeButton(view, R.id.toSet, child.getChangedToTime(), new OnTimeChangedListener() {
            @Override
            public void onTimeChanged(String newTime) {
                child.setChangedToTime(newTime);
                notifyWeekProfileChangedListener();
            }
        });

        int position = relativeChildPosition + 1;

        TextView fromText = (TextView) view.findViewById(R.id.fromText);
        fromText.setText(context.getString(R.string.fromTimetable, position));

        TextView toText = (TextView) view.findViewById(R.id.toText);
        toText.setText(context.getString(R.string.toTimetable, position));

        return view;
    }

    private void setChangeTimeButton(View view, int buttonId, final String currentTime, final OnTimeChangedListener listener) {

        Button setTimeButton = (Button) view.findViewById(buttonId);
        setTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View buttonView) {
                int hours = Integer.valueOf(currentTime.substring(0, 2));
                if (hours == 24) hours = 0;
                int minutes = Integer.valueOf(currentTime.substring(3, 5));

                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minuteOfDay) {
                        String time = timeToTimeString(hourOfDay, minuteOfDay);
                        listener.onTimeChanged(time);
                        context.sendBroadcast(new Intent(Actions.DO_UPDATE));
                    }
                }, hours, minutes, true);

                timePickerDialog.show();
            }
        });
    }
}
