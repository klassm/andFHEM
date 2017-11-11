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
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.List;
import java.util.Locale;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.state.StateAttributeAction;
import li.klass.fhem.adapter.devices.genericui.SpinnerActionRow;
import li.klass.fhem.adapter.devices.genericui.TemperatureChangeTableRow;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.fht.FHTMode;
import li.klass.fhem.room.list.backend.xmllist.XmlListDevice;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.ui.AndroidBug;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.DatePickerUtil;
import li.klass.fhem.util.DateTimeProvider;
import li.klass.fhem.util.EnumUtils;
import li.klass.fhem.util.StateToSet;

import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.adapter.devices.core.generic.detail.actions.devices.FHTDetailActionProvider.MAXIMUM_TEMPERATURE;
import static li.klass.fhem.adapter.devices.core.generic.detail.actions.devices.FHTDetailActionProvider.MINIMUM_TEMPERATURE;
import static li.klass.fhem.ui.AndroidBug.showMessageIfColorStateBugIsEncountered;
import static li.klass.fhem.util.EnumUtils.toStringList;

public class ModeStateOverwrite implements StateAttributeAction {

    private final HolidayShort holidayShort;
    private final ApplicationProperties applicationProperties;

    public ModeStateOverwrite(ApplicationProperties applicationProperties, DateTimeProvider dateTimeProvider) {
        this.applicationProperties = applicationProperties;
        this.holidayShort = new HolidayShort(applicationProperties, dateTimeProvider);
    }

    @Override
    public TableRow createRow(XmlListDevice device, String connectionId, String key, String stateValue, Context context, final ViewGroup parent) {
        int selected = EnumUtils.positionOf(FHTMode.values(), modeFor(stateValue));
        return new SpinnerActionRow(context, null, context.getString(R.string.setMode), toStringList(FHTMode.values()), selected) {

            @Override
            public void onItemSelected(Context context, XmlListDevice device, String connectionId, String item) {
                FHTMode mode = modeFor(item);
                setMode(device, mode, this, parent, context, LayoutInflater.from(context));
            }
        }.createRow(device, connectionId, parent);
    }

    @Override
    public boolean supports(XmlListDevice xmlListDevice) {
        return true;
    }

    private FHTMode modeFor(String stateValue) {
        try {
            return FHTMode.valueOf(stateValue.toUpperCase(Locale.getDefault()));
        } catch (IllegalArgumentException e) {
            return FHTMode.UNKNOWN;
        }
    }


    private void setMode(XmlListDevice device, FHTMode mode, final SpinnerActionRow spinnerActionRow, ViewGroup viewGroup, Context context, LayoutInflater inflater) {
        final Intent intent = new Intent(Actions.DEVICE_SET_SUB_STATES)
                .setClass(context, DeviceIntentService.class)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                .putExtra(BundleExtraKeys.STATES, newArrayList(new StateToSet("mode", mode.name().toLowerCase(Locale.getDefault()))))
                .putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context));

        switch (mode) {
            case UNKNOWN:
                break;
            case HOLIDAY:
                handleHolidayMode(device, spinnerActionRow, intent, context, viewGroup, inflater);
                break;
            case HOLIDAY_SHORT:
                handleHolidayShortMode(device, spinnerActionRow, intent, viewGroup, context, inflater);
                break;
            default:
                context.startService(intent);
                spinnerActionRow.commitSelection();
        }
    }

    private void handleHolidayMode(final XmlListDevice device, final SpinnerActionRow spinnerActionRow,
                                   final Intent intent, final Context context, final ViewGroup parent, final LayoutInflater inflater) {

        showMessageIfColorStateBugIsEncountered(context, new AndroidBug.MessageBugHandler() {
            @Override
            public void defaultAction() {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

                TableLayout contentView = (TableLayout) inflater.inflate(R.layout.fht_holiday_dialog, parent, false);

                final DatePicker datePicker = (DatePicker) contentView.findViewById(R.id.datePicker);
                DatePickerUtil.hideYearField(datePicker);

                TableRow temperatureUpdateRow = (TableRow) contentView.findViewById(R.id.updateRow);

                final TemperatureChangeTableRow temperatureChangeTableRow =
                        new TemperatureChangeTableRow(context, MINIMUM_TEMPERATURE, temperatureUpdateRow,
                                MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE, applicationProperties);
                contentView.addView(temperatureChangeTableRow.createRow(inflater, device));

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
                        @SuppressWarnings("unchecked") List<StateToSet> states = (List<StateToSet>) intent.getSerializableExtra(BundleExtraKeys.STATES);
                        states.add(new StateToSet("desired-temp", "" + temperatureChangeTableRow.getTemperature()));
                        states.add(new StateToSet("holiday1", "" + datePicker.getDayOfMonth()));
                        states.add(new StateToSet("holiday2", "" + (datePicker.getMonth() + 1)));
                        context.startService(intent);

                        spinnerActionRow.commitSelection();
                        dialogInterface.dismiss();
                    }
                });

                dialogBuilder.show();
            }
        });
    }

    private void handleHolidayShortMode(XmlListDevice device, final SpinnerActionRow spinnerActionRow, final Intent intent, final ViewGroup parent,
                                        Context context, LayoutInflater layoutInflater) {

        holidayShort.showDialog(context, parent, layoutInflater, spinnerActionRow, device, intent);
    }

}
