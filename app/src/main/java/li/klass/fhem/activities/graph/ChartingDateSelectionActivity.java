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

package li.klass.fhem.activities.graph;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import li.klass.fhem.R;
import li.klass.fhem.constants.BundleExtraKeys;

public class ChartingDateSelectionActivity extends Activity {

    public static final String TAG = ChartingDateSelectionActivity.class.getName();
    private DateTime startDate = new DateTime();
    private DateTime endDate = new DateTime();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();


        startDate = (DateTime) extras.getSerializable(BundleExtraKeys.START_DATE);
        endDate = (DateTime) extras.getSerializable(BundleExtraKeys.END_DATE);

        Log.i(TAG, "start date " + DATE_FORMATTER.print(startDate) + " " + TIME_FORMATTER.print(startDate));
        Log.i(TAG, "end date " + DATE_FORMATTER.print(endDate) + " " + TIME_FORMATTER.print(endDate));

        setContentView(R.layout.graph_select_day);

        updateDateTextField(R.id.startDate, startDate);
        updateDateTextField(R.id.endDate, endDate);
        updateTimeTextField(R.id.startTime, startDate);
        updateTimeTextField(R.id.endTime, endDate);

        updateOkButtonVisibility();

        final Button startDateButton = (Button) findViewById(R.id.startDateSet);
        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(ChartingDateSelectionActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        startDate = new DateTime(year, month + 1, dayOfMonth, startDate.getHourOfDay(),
                                startDate.getMinuteOfHour());
                        updateDateTextField(R.id.startDate, startDate);
                        updateOkButtonVisibility();
                    }
                }, startDate.getYear(), startDate.getMonthOfYear() - 1, startDate.getDayOfMonth());

                dialog.show();
            }
        });

        Button startTimeButton = (Button) findViewById(R.id.startTimeSet);
        startTimeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                TimePickerDialog dialog = new TimePickerDialog(ChartingDateSelectionActivity.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(),
                                startDate.getDayOfMonth(), hour, minute);
                        updateTimeTextField(R.id.startTime, startDate);
                        updateOkButtonVisibility();
                    }
                }, startDate.getHourOfDay(), startDate.getMinuteOfHour(), true);
                dialog.show();
            }
        });

        final Button endDateButton = (Button) findViewById(R.id.endDateSet);
        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(ChartingDateSelectionActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        endDate = new DateTime(year, month + 1, dayOfMonth, endDate.getHourOfDay(),
                                endDate.getMinuteOfHour());
                        updateDateTextField(R.id.endDate, endDate);
                        updateOkButtonVisibility();
                    }
                }, endDate.getYear(), endDate.getMonthOfYear() - 1, endDate.getDayOfMonth());

                dialog.show();
            }
        });

        Button endTimeButton = (Button) findViewById(R.id.endTimeSet);
        endTimeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                TimePickerDialog dialog = new TimePickerDialog(ChartingDateSelectionActivity.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(),
                                endDate.getDayOfMonth(), hour, minute);
                        updateTimeTextField(R.id.endTime, endDate);
                        updateOkButtonVisibility();
                    }
                }, endDate.getHourOfDay(), endDate.getMinuteOfHour(), true);
                dialog.show();
            }
        });

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK, getIntent());
                getIntent().putExtra(BundleExtraKeys.START_DATE, startDate);
                getIntent().putExtra(BundleExtraKeys.END_DATE, endDate);

                finish();
            }
        });
    }

    private void updateOkButtonVisibility() {
        Button okButton = (Button) findViewById(R.id.okButton);
        if (endDate.isBefore(startDate)) {
            okButton.setVisibility(View.GONE);
            Toast.makeText(this, R.string.startDateAfterEndDateMsg, Toast.LENGTH_SHORT).show();
        } else {
            okButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateDateTextField(int textViewLayoutId, DateTime calendarToSet) {
        TextView layoutItem = (TextView) findViewById(textViewLayoutId);
        layoutItem.setText(DATE_FORMATTER.print(calendarToSet));
    }

    private void updateTimeTextField(int textViewLayoutId, DateTime calendarToSet) {
        TextView layoutItem = (TextView) findViewById(textViewLayoutId);
        layoutItem.setText(TIME_FORMATTER.print(calendarToSet));
    }
}
