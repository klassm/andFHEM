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

package li.klass.fhem.adapter.devices;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import org.joda.time.DateTime;

import java.util.Calendar;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.core.GenericDeviceAdapter;
import li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesSwitchActionRow;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewButtonAction;
import li.klass.fhem.adapter.devices.genericui.SpinnerActionRow;
import li.klass.fhem.adapter.devices.genericui.TemperatureChangeTableRow;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.domain.fht.FHTMode;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.DateFormatUtil;
import li.klass.fhem.util.DatePickerUtil;
import li.klass.fhem.util.EnumUtils;

import static li.klass.fhem.constants.Actions.DEVICE_SET_DAY_TEMPERATURE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_DESIRED_TEMPERATURE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_NIGHT_TEMPERATURE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_WINDOW_OPEN_TEMPERATURE;
import static li.klass.fhem.domain.FHTDevice.MAXIMUM_TEMPERATURE;
import static li.klass.fhem.domain.FHTDevice.MINIMUM_TEMPERATURE;
import static li.klass.fhem.util.EnumUtils.toStringList;

public class FHTAdapter extends GenericDeviceAdapter<FHTDevice> {

    @Inject
    ApplicationProperties applicationProperties;

    public FHTAdapter() {
        super(FHTDevice.class);
    }

    @Override
    protected void afterPropertiesSet() {
        registerFieldListener("desiredTemp", new FieldNameAddedToDetailListener<FHTDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FHTDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new TemperatureChangeTableRow<FHTDevice>(context, device.getDesiredTemp(), fieldTableRow,
                        DEVICE_SET_DESIRED_TEMPERATURE, R.string.desiredTemperature, MINIMUM_TEMPERATURE,
                        MAXIMUM_TEMPERATURE, applicationProperties)
                        .createRow(getInflater(), device));
            }
        });
        registerFieldListener("dayTemperature", new FieldNameAddedToDetailListener<FHTDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FHTDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new TemperatureChangeTableRow<FHTDevice>(context, device.getDayTemperature(), fieldTableRow,
                        DEVICE_SET_DAY_TEMPERATURE, R.string.dayTemperature, MINIMUM_TEMPERATURE,
                        MAXIMUM_TEMPERATURE, applicationProperties)
                        .createRow(getInflater(), device));
            }
        });
        registerFieldListener("nightTemperature", new FieldNameAddedToDetailListener<FHTDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FHTDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new TemperatureChangeTableRow<FHTDevice>(context, device.getNightTemperature(), fieldTableRow,
                        DEVICE_SET_NIGHT_TEMPERATURE, R.string.nightTemperature, MINIMUM_TEMPERATURE,
                        MAXIMUM_TEMPERATURE, applicationProperties)
                        .createRow(getInflater(), device));
            }
        });
        registerFieldListener("windowOpenTemp", new FieldNameAddedToDetailListener<FHTDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FHTDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new TemperatureChangeTableRow<FHTDevice>(context, device.getWindowOpenTemp(), fieldTableRow,
                        DEVICE_SET_WINDOW_OPEN_TEMPERATURE, R.string.windowOpenTemp,
                        MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE, applicationProperties)
                        .createRow(getInflater(), device));
            }
        });

        registerFieldListener("actuator", new FieldNameAddedToDetailListener<FHTDevice>() {
            @Override
            public void onFieldNameAdded(Context context, final TableLayout tableLayout, String field, FHTDevice device, TableRow fieldTableRow) {
                FHTMode mode = device.getHeatingMode();

                int selected = EnumUtils.positionOf(FHTMode.values(), mode);
                tableLayout.addView(new SpinnerActionRow<FHTDevice>(context, R.string.mode, R.string.setMode, toStringList(FHTMode.values()), selected) {

                    @Override
                    public void onItemSelected(Context context, FHTDevice device, String item) {
                        FHTMode mode = FHTMode.valueOf(item);
                        setMode(device, mode, this, tableLayout);
                    }
                }.createRow(device, tableLayout));
            }
        });

        detailActions.add(new DeviceDetailViewButtonAction<FHTDevice>(R.string.timetable) {
            @Override
            public void onButtonClick(Context context, FHTDevice device) {
                Intent intent = new Intent(Actions.SHOW_FRAGMENT);
                intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.FROM_TO_WEEK_PROFILE);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                getContext().sendBroadcast(intent);
            }
        });

        detailActions.add(new DeviceDetailViewButtonAction<FHTDevice>(R.string.requestRefresh) {
            @Override
            public void onButtonClick(Context context, FHTDevice device) {
                Intent intent = new Intent(Actions.DEVICE_REFRESH_VALUES);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                getContext().startService(intent);
            }
        });

        detailActions.add(new AvailableTargetStatesSwitchActionRow<FHTDevice>());
    }

    private void setMode(FHTDevice device, FHTMode mode, final SpinnerActionRow<FHTDevice> spinnerActionRow, TableLayout tableLayout) {
        final Intent intent = new Intent(Actions.DEVICE_SET_MODE);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.DEVICE_MODE, mode);

        switch (mode) {
            case UNKNOWN:
                break;
            case AUTO:
                break;
            case MANUAL:
                break;
            case HOLIDAY:

                handleHolidayMode(device, spinnerActionRow, intent, tableLayout);

                break;

            case HOLIDAY_SHORT:

                handleHolidayShortMode(device, spinnerActionRow, intent, tableLayout);

                break;
            default:
                getContext().startService(intent);
                spinnerActionRow.commitSelection();
        }
    }

    private void handleHolidayMode(FHTDevice device, final SpinnerActionRow<FHTDevice> spinnerActionRow,
                                   final Intent intent, TableLayout tableLayout) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        TableLayout contentView = (TableLayout) getInflater().inflate(R.layout.fht_holiday_dialog, tableLayout, false);

        final DatePicker datePicker = (DatePicker) contentView.findViewById(R.id.datePicker);
        DatePickerUtil.hideYearField(datePicker);

        TableRow temperatureUpdateRow = (TableRow) contentView.findViewById(R.id.updateRow);

        final TemperatureChangeTableRow<FHTDevice> temperatureChangeTableRow =
                new TemperatureChangeTableRow<>(getContext(), FHTDevice.MINIMUM_TEMPERATURE, temperatureUpdateRow,
                        FHTDevice.MINIMUM_TEMPERATURE, FHTDevice.MAXIMUM_TEMPERATURE, applicationProperties);
        contentView.addView(temperatureChangeTableRow.createRow(getInflater(), device));

        dialogBuilder.setView(contentView);

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
                intent.putExtra(BundleExtraKeys.DEVICE_TEMPERATURE, temperatureChangeTableRow.getTemperature());
                intent.putExtra(BundleExtraKeys.DEVICE_HOLIDAY1, datePicker.getDayOfMonth());
                intent.putExtra(BundleExtraKeys.DEVICE_HOLIDAY2, datePicker.getMonth() + 1);

                getContext().startService(intent);

                spinnerActionRow.commitSelection();
                dialogInterface.dismiss();
            }
        });

        dialogBuilder.show();
    }

    private void handleHolidayShortMode(FHTDevice device, final SpinnerActionRow<FHTDevice> spinnerActionRow, final Intent intent, TableLayout tableLayout) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        final TableLayout contentView = (TableLayout) getInflater().inflate(R.layout.fht_holiday_short_dialog, tableLayout, false);
        dialogBuilder.setView(contentView);

        TableRow temperatureUpdateRow = (TableRow) contentView.findViewById(R.id.updateTemperatureRow);
        final TimePicker timePicker = (TimePicker) contentView.findViewById(R.id.timePicker);

        DateTime now = new DateTime();
        timePicker.setIs24HourView(true);
        timePicker.setCurrentMinute(0);
        timePicker.setCurrentHour(now.getHourOfDay());

        TextView endTimeView = (TextView) contentView.findViewById(R.id.endTimeValue);
        endTimeView.setText(DateFormatUtil.toReadable(now));

        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            private int lastMinute = 0;

            @Override
            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                DateTime switchDate = new DateTime();
                if (holidayShortIsTomorrow(timePicker)) {
                    switchDate = switchDate.plusDays(1);
                }
                String switchDateString = DateFormatUtil.toReadable(switchDate);

                TextView endTimeView = (TextView) contentView.findViewById(R.id.endTimeValue);
                endTimeView.setText(switchDateString);

                int rest = minute % 10;
                if (rest == 0) return;

                int newMinute;
                if (minute < lastMinute) {
                    newMinute = minute - rest;
                } else {
                    newMinute = minute + (10 - rest);
                }
                if (newMinute < 0 || newMinute >= 60) newMinute = 0;

                lastMinute = newMinute;
                timePicker.setCurrentMinute(newMinute);
            }
        });

        final TemperatureChangeTableRow<FHTDevice> temperatureChangeTableRow =
                new TemperatureChangeTableRow<>(getContext(), FHTDevice.MINIMUM_TEMPERATURE, temperatureUpdateRow,
                        FHTDevice.MINIMUM_TEMPERATURE, FHTDevice.MAXIMUM_TEMPERATURE, applicationProperties);
        contentView.addView(temperatureChangeTableRow.createRow(getInflater(), device));


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
                intent.putExtra(BundleExtraKeys.DEVICE_TEMPERATURE, temperatureChangeTableRow.getTemperature());
                intent.putExtra(BundleExtraKeys.DEVICE_HOLIDAY1, extraxtHolidayShortHoliday1ValueFrom(timePicker));

                Calendar now = Calendar.getInstance();
                if (holidayShortIsTomorrow(timePicker)) {
                    now.add(Calendar.DAY_OF_MONTH, 1);
                }
                intent.putExtra(BundleExtraKeys.DEVICE_HOLIDAY2, now.get(Calendar.DAY_OF_MONTH));

                getContext().startService(intent);

                spinnerActionRow.commitSelection();
                dialogInterface.dismiss();
            }
        });
        dialogBuilder.show();
    }

    public static boolean holidayShortIsTomorrow(TimePicker timePicker) {
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();

        Calendar now = Calendar.getInstance();
        int currentMinutes = (now.get(Calendar.MINUTE) / 10) * 10;
        int currentHour = now.get(Calendar.HOUR_OF_DAY);

        return holidayShortIsTomorrow(currentHour, currentMinutes, hour, minute);
    }

    public static int extraxtHolidayShortHoliday1ValueFrom(TimePicker timePicker) {
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();

        return caclulateHolidayShortHoliday1ValueFrom(hour, minute);
    }

    public static boolean holidayShortIsTomorrow(int currentHour, int currentMinutes, int selectedHour, int selectedMinutes) {
        if (selectedHour < currentHour) {
            return true;
        } else if (selectedHour == currentHour) {
            return currentMinutes > selectedMinutes;
        }

        return false;
    }

    public static int caclulateHolidayShortHoliday1ValueFrom(int hour, int minute) {
        return hour * 6 + minute / 10;
    }
}
