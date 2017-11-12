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

package li.klass.fhem.adapter.devices.core.generic.detail.actions.devices.fht;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import org.joda.time.DateTime;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.SpinnerActionRow;
import li.klass.fhem.adapter.devices.genericui.TemperatureChangeTableRow;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.ui.AndroidBug;
import li.klass.fhem.update.backend.xmllist.XmlListDevice;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.DateFormatUtil;
import li.klass.fhem.util.DateTimeProvider;
import li.klass.fhem.util.StateToSet;
import li.klass.fhem.widget.FallbackTimePicker;

import static li.klass.fhem.adapter.devices.core.generic.detail.actions.devices.FHTDetailActionProvider.MAXIMUM_TEMPERATURE;
import static li.klass.fhem.adapter.devices.core.generic.detail.actions.devices.FHTDetailActionProvider.MINIMUM_TEMPERATURE;
import static li.klass.fhem.ui.AndroidBug.handleColorStateBug;

public class HolidayShort {

    private final ApplicationProperties applicationProperties;
    private final DateTimeProvider dateTimeProvider;

    private int hour = 0;
    private int minute = 0;

    public HolidayShort(ApplicationProperties applicationProperties, DateTimeProvider dateTimeProvider) {
        this.applicationProperties = applicationProperties;
        this.dateTimeProvider = dateTimeProvider;
    }

    public void showDialog(final Context context, final ViewGroup parent, final LayoutInflater layoutInflater,
                           final SpinnerActionRow spinnerActionRow, final XmlListDevice device, final Intent switchIntent) {
        final DateTime now = dateTimeProvider.now();
        final TableLayout contentView = (TableLayout) handleColorStateBug(new AndroidBug.BugHandler() {
            @Override
            public View bugEncountered() {
                final TableLayout contentView = (TableLayout) layoutInflater.inflate(R.layout.fht_holiday_short_dialog_android_bug, parent, false);

                final FallbackTimePicker timePicker = contentView.findViewById(R.id.timePicker);
                timePicker.setMinutes(0);
                timePicker.setHours(now.getHourOfDay());

                timePicker.setOnValueChangedListener(new FallbackTimePicker.OnValueChangedListener() {
                    @Override
                    public void onValueChanged(int hours, int minutes) {
                        HolidayShort.this.hour = hours;
                        HolidayShort.this.minute = minutes;

                        updateHolidayShortEndTime(contentView);
                    }
                });

                return contentView;
            }

            @Override
            public View defaultAction() {
                final TableLayout contentView = (TableLayout) layoutInflater.inflate(R.layout.fht_holiday_short_dialog, parent, false);

                final TimePicker timePicker = contentView.findViewById(R.id.timePicker);
                timePicker.setIs24HourView(true);
                timePicker.setCurrentMinute(0);
                timePicker.setCurrentHour(now.getHourOfDay());


                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                        HolidayShort.this.hour = hourOfDay;
                        HolidayShort.this.minute = minute;

                        updateHolidayShortEndTime(contentView);
                    }
                });

                return contentView;
            }
        });
        updateHolidayShortEndTime(contentView);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);


        TableRow temperatureUpdateRow = contentView.findViewById(R.id.updateTemperatureRow);
        final TemperatureChangeTableRow temperatureChangeTableRow =
                new TemperatureChangeTableRow(context, MINIMUM_TEMPERATURE, temperatureUpdateRow,
                        MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE, applicationProperties);
        contentView.addView(temperatureChangeTableRow.createRow(layoutInflater, device));

        dialogBuilder.setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                spinnerActionRow.revertSelection();
                dialogInterface.dismiss();
            }
        });

        dialogBuilder.setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int item) {
                DateTime switchDate = holiday1SwitchTimeFor(hour, minute);

                @SuppressWarnings("unchecked") List<StateToSet> states = (List<StateToSet>) switchIntent.getSerializableExtra(BundleExtraKeys.STATES);
                states.add(new StateToSet("desired-temp", "" + temperatureChangeTableRow.getTemperature()));
                states.add(new StateToSet("holiday1", "" + extractHolidayShortHoliday1ValueFrom(switchDate)));
                states.add(new StateToSet("holiday2", "" + switchDate.getDayOfMonth()));
                context.startService(switchIntent);

                spinnerActionRow.commitSelection();
                dialogInterface.dismiss();
            }
        });
        dialogBuilder.setView(contentView);
        dialogBuilder.show();
    }

    private void updateHolidayShortEndTime(TableLayout contentView) {
        DateTime switchDate = holiday1SwitchTimeFor(hour, minute);
        String switchDateString = DateFormatUtil.toReadable(switchDate);

        ((TextView) contentView.findViewById(R.id.endTimeValue)).setText(switchDateString);
    }

    DateTime holiday1SwitchTimeFor(int hourOfDay, int minute) {

        int newMinute = (int) ((Math.round(minute / 10.0) * 10) % 60);
        if (newMinute == 0) {
            hourOfDay += minute > 30 ? 1 : 0;
        }
        hourOfDay %= 24;

        DateTime now = dateTimeProvider.now();
        DateTime switchTime = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), hourOfDay, newMinute);

        if (holidayShortIsTomorrow(switchTime, now)) {
            switchTime = switchTime.plusDays(1);
        }
        return switchTime;
    }

    int extractHolidayShortHoliday1ValueFrom(DateTime dateTime) {
        return calculateHolidayShortHoliday1ValueFrom(dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
    }

    boolean holidayShortIsTomorrow(DateTime switchTime, DateTime baseline) {
        int currentMinute = baseline.getHourOfDay() * 60 + baseline.getMinuteOfHour();
        int switchMinute = switchTime.getHourOfDay() * 60 + switchTime.getMinuteOfHour();

        return switchMinute < currentMinute;
    }

    int calculateHolidayShortHoliday1ValueFrom(int hour, int minute) {
        return hour * 6 + minute / 10;
    }
}
