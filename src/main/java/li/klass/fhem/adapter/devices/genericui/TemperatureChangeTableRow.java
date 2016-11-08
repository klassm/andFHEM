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

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.widget.TableRow;
import android.widget.TextView;

import li.klass.fhem.R;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.service.room.xmllist.XmlListDevice;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.device.DeviceActionUtil;

import static com.google.common.base.Preconditions.checkNotNull;
import static li.klass.fhem.util.ValueDescriptionUtil.appendTemperature;

public class TemperatureChangeTableRow extends SeekBarActionRowFullWidthAndButton {
    private double newTemperature;
    private String intentAction;
    private int valueStringId;
    private Context context;
    private boolean sendIntents = true;
    private ApplicationProperties applicationProperties;

    public TemperatureChangeTableRow(Context context, double initialTemperature, TableRow updateTableRow,
                                     double minTemperature, double maxTemperature,
                                     ApplicationProperties applicationProperties) {
        this(context, initialTemperature, updateTableRow, null, -1, minTemperature, maxTemperature, applicationProperties);
        checkNotNull(applicationProperties);
        sendIntents = false;
    }

    public TemperatureChangeTableRow(Context context, double initialTemperature, TableRow updateTableRow,
                                     String intentAction, int valueStringId, double minTemperature,
                                     double maxTemperature, ApplicationProperties applicationProperties) {
        super(context, (float) initialTemperature, 0.5f, (float) minTemperature, (float) maxTemperature, updateTableRow);

        this.intentAction = intentAction;
        this.valueStringId = valueStringId;
        this.context = context;
        this.applicationProperties = applicationProperties;
        this.newTemperature = initialProgress;

        updateView.setText(appendTemperature(initialTemperature));
    }

    @Override
    public void onProgressChanged(TextView updateView, Context context, XmlListDevice device, float progress) {
        this.newTemperature = progress;
        updateView.setText(appendTemperature(newTemperature));
    }

    @Override
    public void onStopTrackingTouch(final Context seekBarContext, final XmlListDevice device, float progress) {
        if (!sendIntents) return;
        if (progress == initialProgress) return;

        initialProgress = progress;

        String confirmationMessage = createConfirmationText(valueStringId, newTemperature);
        DeviceActionUtil.showConfirmation(context, (dialogInterface, i) -> setValue(device, newTemperature), confirmationMessage);
    }

    private String createConfirmationText(int attributeStringId, double newTemperature) {
        Resources resources = context.getResources();

        String attributeText = resources.getString(attributeStringId);
        String temperatureText = appendTemperature(newTemperature);

        String text = resources.getString(R.string.areYouSureText);
        return String.format(text, attributeText, temperatureText);
    }

    private void setValue(XmlListDevice device, double newValue) {
        Intent intent = new Intent(intentAction)
                .setClass(context, DeviceIntentService.class)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                .putExtra(BundleExtraKeys.DEVICE_TEMPERATURE, newValue);
        onIntentCreation(intent);

        context.startService(intent);

        updateView.setText(appendTemperature(newValue));
    }

    protected void onIntentCreation(Intent intent) {
    }

    @Override
    public void onButtonSetValue(XmlListDevice device, int value) {
        setValue(device, value);
    }

    @Override
    protected ApplicationProperties getApplicationProperties() {
        return applicationProperties;
    }

    public double getTemperature() {
        return newTemperature;
    }
}
