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
import android.widget.*;
import li.klass.fhem.R;
import li.klass.fhem.constants.BundleExtraKeys;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ChartingDateSelectionActivity extends Activity{

    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();


        startDate.setTime((Date) extras.getSerializable(BundleExtraKeys.START_DATE));
        endDate.setTime((Date) extras.getSerializable(BundleExtraKeys.END_DATE));

        Log.e(ChartingDateSelectionActivity.class.getName(), "start date " + dateFormat.format(startDate.getTime()) + " " + timeFormat.format(startDate.getTime()));
        Log.e(ChartingDateSelectionActivity.class.getName(), "end date " + dateFormat.format(endDate.getTime()) + " " + timeFormat.format(endDate.getTime()));
        setContentView(R.layout.graph_select_day);

        updateDateTextField(R.id.startDate, startDate);
        updateDateTextField(R.id.endDate, endDate);
        updateTimeTextField(R.id.startTime, startDate);
        updateTimeTextField(R.id.endTime, endDate);

        updateOkButtonVisibility();

        Button startDateButton = (Button) findViewById(R.id.startDateSet);
        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(ChartingDateSelectionActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        startDate.set(year, month, dayOfMonth);
                        updateDateTextField(R.id.startDate, startDate);
                        updateOkButtonVisibility();
                    }
                }, startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH));

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
                        startDate.set(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH),
                                startDate.get(Calendar.DAY_OF_MONTH), hour, minute);
                        updateTimeTextField(R.id.startTime, startDate);
                        updateOkButtonVisibility();
                    }
                }, startDate.get(Calendar.HOUR_OF_DAY), startDate.get(Calendar.MINUTE), true);
                dialog.show();
            }
        });

        Button endDateButton = (Button) findViewById(R.id.endDateSet);
        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(ChartingDateSelectionActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        endDate.set(year, month, dayOfMonth);
                        updateDateTextField(R.id.endDate, endDate);
                        updateOkButtonVisibility();
                    }
                }, endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH), endDate.get(Calendar.DAY_OF_MONTH));

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
                        endDate.set(endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH),
                                endDate.get(Calendar.DAY_OF_MONTH), hour, minute);
                        updateTimeTextField(R.id.endTime, endDate);
                        updateOkButtonVisibility();
                    }
                }, endDate.get(Calendar.HOUR_OF_DAY), endDate.get(Calendar.MINUTE), true);
                dialog.show();
            }
        });

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK, getIntent());
                getIntent().putExtra(BundleExtraKeys.START_DATE, startDate.getTime());
                getIntent().putExtra(BundleExtraKeys.END_DATE, endDate.getTime());

                finish();
            }
        });
    }

    private void updateOkButtonVisibility() {
        Button okButton = (Button) findViewById(R.id.okButton);
        if (endDate.before(startDate)) {
            okButton.setVisibility(View.GONE);
            Toast.makeText(this, R.string.startDateAfterEndDateMsg, Toast.LENGTH_SHORT).show();
        } else {
            okButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateDateTextField(int textViewLayoutId, Calendar calendarToSet) {
        TextView layoutItem = (TextView) findViewById(textViewLayoutId);
        layoutItem.setText(dateFormat.format(calendarToSet.getTime()));
    }

    private void updateTimeTextField(int textViewLayoutId, Calendar calendarToSet) {
        TextView layoutItem = (TextView) findViewById(textViewLayoutId);
        layoutItem.setText(timeFormat.format(calendarToSet.getTime()));
    }
}
