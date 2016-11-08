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

import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.Calendar;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DimmableAdapter;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.genericui.ButtonActionRow;
import li.klass.fhem.adapter.devices.genericui.OldColorPickerRow;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.DummyDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.util.StringUtil;

public class DummyAdapter extends DimmableAdapter {
    @Inject
    StateUiService stateUiService;

    public DummyAdapter() {
        super();
    }

    @Override
    protected void inject(ApplicationComponent daggerComponent) {
        daggerComponent.inject(this);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        registerFieldListener("state", new FieldNameAddedToDetailListener() {
            @Override
            public void onFieldNameAdded(final Context context, TableLayout tableLayout,
                                         String field, final FhemDevice device,
                                         TableRow fieldTableRow) {
                tableLayout.addView(new ButtonActionRow(context, R.string.set) {

                    @Override
                    protected void onButtonClick() {
                        Calendar now = Calendar.getInstance();
                        TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute) -> {
                            String hourOut = "" + hourOfDay;
                            if (hourOfDay < 10) hourOut = "0" + hourOut;

                            String minuteOut = "" + minute;
                            if (minute < 10) minuteOut = "0" + minuteOut;

                            stateUiService.setState(device, hourOut + ":" + minuteOut, context);
                        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                        timePickerDialog.show();
                    }
                }.createRow(getInflater()));
            }

            @Override
            public boolean supportsDevice(FhemDevice device) {
                return ((DummyDevice) device).isTimerDevice();
            }
        });

        registerFieldListener("rgbDesc", new FieldNameAddedToDetailListener() {
            @Override
            public boolean supportsDevice(FhemDevice device) {
                return ((DummyDevice) device).getRgbDesc() != null;
            }

            @Override
            public void onFieldNameAdded(final Context context, TableLayout tableLayout, String field,
                                         final FhemDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new OldColorPickerRow(((DummyDevice) device).getRGBColor(), R.string.hue) {
                    @Override
                    public void onColorChange(int color) {
                        String targetHexString = StringUtil.prefixPad(
                                Integer.toHexString(color),
                                "0", 6
                        );

                        stateUiService.setSubState(device, "rgb", targetHexString, context);
                    }
                }.createRow(context, getInflater(), tableLayout));
            }
        });
    }
}
