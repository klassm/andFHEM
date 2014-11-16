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
import android.widget.TableRow;

import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.setlist.SetListSliderValue;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.util.ApplicationProperties;

public class StateChangingSeekBarFullWidth<D extends Device<D>> extends SeekBarActionRowFullWidthAndButton<D> {

    private String commandAttribute;
    private ApplicationProperties applicationProperties;

    public StateChangingSeekBarFullWidth(Context context, int initialProgress, int maximumProgress,
                                         String commandAttribute, ApplicationProperties applicationProperties) {
        this(context, initialProgress, 0, maximumProgress, commandAttribute, applicationProperties);
    }

    public StateChangingSeekBarFullWidth(Context context, int initialProgress, int minimumProgress, int maximumProgress, String commandAttribute,
                                         ApplicationProperties applicationProperties) {
        super(context, initialProgress, minimumProgress, maximumProgress);
        this.commandAttribute = commandAttribute;
        this.applicationProperties = applicationProperties;
    }

    public StateChangingSeekBarFullWidth(Context context, int initialProgress, SetListSliderValue sliderValue,
                                         String commandAttribute, ApplicationProperties applicationProperties) {
        this(context, initialProgress, sliderValue.getStart(), sliderValue.getStop(),
                commandAttribute, applicationProperties);
    }

    public StateChangingSeekBarFullWidth(Context context, int initialProgress, SetListSliderValue sliderValue,
                                         String commandAttribute, TableRow updateRow,
                                         ApplicationProperties applicationProperties) {
        this(context, initialProgress, sliderValue.getStart(), sliderValue.getStop(),
                commandAttribute, applicationProperties);
        setUpdateRow(updateRow);
    }


    @Override
    public void onButtonSetValue(D device, int value) {
        onStopTrackingTouch(context, device, value);
    }

    @Override
    public void onStopTrackingTouch(Context context, D device, int progress) {
        Intent intent = new Intent(Actions.DEVICE_SET_SUB_STATE);
        intent.setClass(context, DeviceIntentService.class);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.STATE_NAME, commandAttribute);
        intent.putExtra(BundleExtraKeys.STATE_VALUE, progress + "");
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context));

        context.startService(intent);
    }

    @Override
    protected ApplicationProperties getApplicationProperties() {
        return applicationProperties;
    }
}
