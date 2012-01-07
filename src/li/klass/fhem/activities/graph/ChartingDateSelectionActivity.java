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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import li.klass.fhem.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ChartingDateSelectionActivity extends Activity{
    
    public static final String INTENT_DEVICE_NAME = "deviceName";
    public static final String INTENT_START_DATE = "startDate";
    public static final String INTENT_END_DATE = "endDate";



    private String deviceName;
    
    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        deviceName = extras.getString(INTENT_DEVICE_NAME);
        startDate.setTime((Date) extras.getSerializable(INTENT_START_DATE));
        endDate.setTime((Date) extras.getSerializable(INTENT_END_DATE));

        setContentView(R.layout.graph_select_day);

        updateTimeTextField(R.id.startDate, startDate);
        updateTimeTextField(R.id.endDate, endDate);
        updateOkButtonVisibility();

        Button startDateButton = (Button) findViewById(R.id.startDateSet);
        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(ChartingDateSelectionActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        startDate.set(year, month, dayOfMonth);
                        updateTimeTextField(R.id.startDate, startDate);
                        updateOkButtonVisibility();
                    }
                }, startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH));

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
                        updateTimeTextField(R.id.endDate, endDate);
                        updateOkButtonVisibility();
                    }
                }, endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH), endDate.get(Calendar.DAY_OF_MONTH));

                dialog.show();
            }
        });

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK, getIntent());
                getIntent().putExtra(INTENT_START_DATE, startDate.getTime());
                getIntent().putExtra(INTENT_END_DATE, endDate.getTime());

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
    
    private void updateTimeTextField(int textViewLayoutId, Calendar calendarToSet) {
        TextView layoutItem = (TextView) findViewById(textViewLayoutId);
        layoutItem.setText(dateFormat.format(calendarToSet.getTime()));
    }


}
