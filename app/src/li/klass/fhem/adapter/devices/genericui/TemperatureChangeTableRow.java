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

package li.klass.fhem.adapter.devices.genericui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.widget.TableRow;
import android.widget.TextView;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.device.DeviceActionUtil;

public class TemperatureChangeTableRow<D extends Device<D>> extends SeekBarActionRowFullWidthAndButton<D> {
    private final TextView updateView;
    private double newTemperature;
    private String intentAction;
    private int valueStringId;
    private Context context;
    private double minTemperature;
    private boolean sendIntents = true;

    public TemperatureChangeTableRow(Context context, double initialTemperature, TableRow updateTableRow,
                                     double minTemperature, double maxTemperature) {
        this(context, initialTemperature, updateTableRow, null, -1, minTemperature, maxTemperature);
        sendIntents = false;
    }

    public TemperatureChangeTableRow(Context context, double initialTemperature, TableRow updateTableRow,
                                     String intentAction, int valueStringId, double minTemperature, double maxTemperature) {
        super(context, temperatureToDimProgress(initialTemperature, minTemperature),
                temperatureToDimProgress(maxTemperature, minTemperature));

        updateView = (TextView) updateTableRow.findViewById(R.id.value);
        this.minTemperature = minTemperature;
        this.intentAction = intentAction;
        this.valueStringId = valueStringId;
        this.context = context;
    }

    @Override
    public void onProgressChanged(Context context, D device, int progress) {
        this.newTemperature = dimProgressToTemperature(progress, minTemperature);
        updateView.setText(ValueDescriptionUtil.appendTemperature(newTemperature));
    }

    @Override
    public void onStopTrackingTouch(final Context seekBarContext, final D device, int progress) {
        if (! sendIntents) return;

        String confirmationMessage = createConfirmationText(valueStringId, newTemperature);
        DeviceActionUtil.showConfirmation(context, new Dialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setValue(device, newTemperature);
            }
        }, confirmationMessage);
    }

    @Override
    public void onButtonSetValue(D device, int value) {
        setValue(device, value);
    }

    private void setValue(D device, double newValue) {
        Intent intent = new Intent(intentAction);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.DEVICE_TEMPERATURE, newValue);
        context.startService(intent);

        updateView.setText(ValueDescriptionUtil.appendTemperature(newValue));
    }

    private String createConfirmationText(int attributeStringId, double newTemperature) {
        Context context = AndFHEMApplication.getContext();
        Resources resources = context.getResources();

        String attributeText = resources.getString(attributeStringId);
        String temperatureText = ValueDescriptionUtil.appendTemperature(newTemperature);

        String text = resources.getString(R.string.areYouSureText);
        return String.format(text, attributeText, temperatureText);
    }

    public double getTemperature() {
        return newTemperature;
    }

    public static int temperatureToDimProgress(double temperature, double minTemperature) {
        return (int) ((temperature - minTemperature) / 0.5);
    }

    public static double dimProgressToTemperature(double progress, double minTemperature) {
        return minTemperature + (progress * 0.5);
    }
}
