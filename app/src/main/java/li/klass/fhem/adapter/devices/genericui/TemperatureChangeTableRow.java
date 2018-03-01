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
import android.support.annotation.NonNull;
import android.widget.TableRow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import li.klass.fhem.R;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.update.backend.xmllist.XmlListDevice;
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

    public TemperatureChangeTableRow(Context context, double initialTemperature, TableRow updateTableRow,
                                     double minTemperature, double maxTemperature,
                                     ApplicationProperties applicationProperties) {
        this(context, initialTemperature, updateTableRow, null, -1, minTemperature, maxTemperature, applicationProperties);
        checkNotNull(applicationProperties);
        sendIntents = false;
    }

    private TemperatureChangeTableRow(Context context, double initialTemperature, TableRow updateTableRow,
                                      String intentAction, int valueStringId, double minTemperature,
                                      double maxTemperature, ApplicationProperties applicationProperties) {
        super(context, initialTemperature, 0.5, minTemperature, maxTemperature, updateTableRow, applicationProperties);

        this.intentAction = intentAction;
        this.valueStringId = valueStringId;
        this.context = context;
        this.newTemperature = getInitialProgress();

        getUpdateView().setText(appendTemperature(initialTemperature));
    }

    @NotNull
    @Override
    public String toUpdateText(@Nullable XmlListDevice device, double progress) {
        return appendTemperature(progress);
    }

    @Override
    public void onProgressChange(@NonNull final Context seekBarContext, final XmlListDevice device, final double progress) {
        if (!sendIntents) return;
        if (progress == getInitialProgress()) return;

        setInitialProgress(progress);

        String confirmationMessage = createConfirmationText(valueStringId, progress);
        DeviceActionUtil.INSTANCE.showConfirmation(context, new Dialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setValue(device, progress);
            }
        }, confirmationMessage);
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

        context.startService(intent);

        getUpdateView().setText(appendTemperature(newValue));
    }

    public double getTemperature() {
        return newTemperature;
    }
}
