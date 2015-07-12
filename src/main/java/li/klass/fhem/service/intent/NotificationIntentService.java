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

package li.klass.fhem.service.intent;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.util.Map;

import javax.inject.Inject;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.NotificationService;

import static li.klass.fhem.constants.BundleExtraKeys.NOTIFICATION_UPDATES;

public class NotificationIntentService extends ConvenientIntentService {

    @Inject
    NotificationService notificationService;


    public NotificationIntentService() {
        super(NotificationIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        String deviceName = intent.getStringExtra(BundleExtraKeys.DEVICE_NAME);

        if (intent.getAction().equals(Actions.NOTIFICATION_SET_FOR_DEVICE)) {
            int updateType = intent.getIntExtra(BundleExtraKeys.NOTIFICATION_UPDATES, 0);
            notificationService.setDeviceNotification(deviceName, updateType, this);
        } else if (intent.getAction().equals(Actions.NOTIFICATION_TRIGGER)) {
            @SuppressWarnings("unchecked")
            Map<String, String> updateMap = (Map<String, String>) intent.getSerializableExtra(BundleExtraKeys.UPDATE_MAP);
            FhemDevice<?> device = (FhemDevice<?>) intent.getSerializableExtra(BundleExtraKeys.DEVICE);
            boolean vibrate = intent.getBooleanExtra(BundleExtraKeys.VIBRATE, false);

            notificationService.deviceNotification(deviceName, updateMap, device, vibrate, this);
        } else if (intent.getAction().equals(Actions.NOTIFICATION_GET_FOR_DEVICE)) {
            int value = notificationService.forDevice(this, deviceName);

            Bundle result = new Bundle();
            result.putInt(NOTIFICATION_UPDATES, value);

            resultReceiver.send(ResultCodes.SUCCESS, result);
        }
        return STATE.SUCCESS;
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }
}
