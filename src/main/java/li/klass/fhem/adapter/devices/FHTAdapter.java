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
import android.os.Build;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import org.joda.time.DateTime;

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
import li.klass.fhem.service.DateService;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.DateFormatUtil;
import li.klass.fhem.util.DatePickerUtil;
import li.klass.fhem.util.DialogUtil;
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

    @Inject
    DateService dateService;

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
                intent.setClass(context, DeviceIntentService.class);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                getContext().startService(intent);
            }
        });

        detailActions.add(new AvailableTargetStatesSwitchActionRow<FHTDevice>());
    }

    private void setMode(FHTDevice device, FHTMode mode, final SpinnerActionRow<FHTDevice> spinnerActionRow, TableLayout tableLayout) {
        final Intent intent = new Intent(Actions.DEVICE_SET_MODE)
                .setClass(getContext(), DeviceIntentService.class)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                .putExtra(BundleExtraKeys.DEVICE_MODE, mode);

        switch (mode) {
            case UNKNOWN:
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
        if (showAndroidBugDialogIfRequired()) {
            return;
        }
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
        if (showAndroidBugDialogIfRequired()) {
            return;
        }
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        final TableLayout contentView = (TableLayout) getInflater().inflate(R.layout.fht_holiday_short_dialog, tableLayout, false);
        dialogBuilder.setView(contentView);

        TableRow temperatureUpdateRow = (TableRow) contentView.findViewById(R.id.updateTemperatureRow);
        final TimePicker timePicker = (TimePicker) contentView.findViewById(R.id.timePicker);

        DateTime now = dateService.now();
        timePicker.setIs24HourView(true);
        timePicker.setCurrentMinute(0);
        timePicker.setCurrentHour(now.getHourOfDay());

        updateHolidayShortEndTime(timePicker, contentView);

        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                updateHolidayShortEndTime(timePicker, contentView);
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
                DateTime switchDate = holiday1SwitchTimeFor(timePicker);

                intent.putExtra(BundleExtraKeys.DEVICE_TEMPERATURE, temperatureChangeTableRow.getTemperature());
                intent.putExtra(BundleExtraKeys.DEVICE_HOLIDAY1, extractHolidayShortHoliday1ValueFrom(switchDate));
                intent.putExtra(BundleExtraKeys.DEVICE_HOLIDAY2, switchDate.getDayOfMonth());
                getContext().startService(intent);

                spinnerActionRow.commitSelection();
                dialogInterface.dismiss();
            }
        });
        dialogBuilder.show();
    }

    private void updateHolidayShortEndTime(TimePicker timePicker, TableLayout contentView) {
        DateTime switchDate = holiday1SwitchTimeFor(timePicker);
        String switchDateString = DateFormatUtil.toReadable(switchDate);

        ((TextView) contentView.findViewById(R.id.endTimeValue)).setText(switchDateString);
    }

    DateTime holiday1SwitchTimeFor(TimePicker timePicker) {
        int minute = timePicker.getCurrentMinute();
        int hourOfDay = timePicker.getCurrentHour();

        int newMinute = (int) ((Math.round(minute / 10.0) * 10) % 60);
        if (newMinute == 0) {
            hourOfDay += minute > 30 ? 1 : 0;
        }
        hourOfDay %= 24;

        DateTime now = dateService.now();
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

    private boolean showAndroidBugDialogIfRequired() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DialogUtil.showAlertDialog(getContext(), R.string.androidBugDialogDatePickerTitle, R.string.androidBugDialogDatePickerContent);
            return true;
        }
        return false;
    }
}
