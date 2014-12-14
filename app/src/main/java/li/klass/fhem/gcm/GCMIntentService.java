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

package li.klass.fhem.gcm;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.activities.AndFHEMMainActivity;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.service.intent.RoomListIntentService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.NotificationUtil;
import li.klass.fhem.util.StringUtil;
import li.klass.fhem.util.Tasker;

import static com.google.common.collect.Maps.newHashMap;
import static li.klass.fhem.constants.PreferenceKeys.GCM_PROJECT_ID;
import static li.klass.fhem.constants.PreferenceKeys.GCM_REGISTRATION_ID;

public class GCMIntentService extends GCMBaseIntentService {
    private static final String TAG = GCMIntentService.class.getName();

    @Override
    protected void onRegistered(Context context, String registrationId) {
        ApplicationProperties applicationProperties = AndFHEMApplication.getApplication().getGraph().get(ApplicationProperties.class);
        applicationProperties.setSharedPreference(GCM_REGISTRATION_ID, registrationId);
        Log.i(TAG, "Device registered: regId = " + registrationId);

        Intent intent = new Intent(Actions.GCM_REGISTERED);
        intent.putExtra(BundleExtraKeys.GCM_REGISTRATION_ID, registrationId);
        sendBroadcast(intent);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            GCMRegistrar.unregister(this);
        } else {
            Log.i(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) return;
        if (!extras.containsKey("type") || !extras.containsKey("source")) {
            Log.i(TAG, "received GCM message, but doesn't fit required fields");
            return;
        }

        String type = extras.getString("type");
        if ("message".equalsIgnoreCase(type)) {
            handleMessage(extras);
        } else if ("notify".equalsIgnoreCase(type) || StringUtil.isBlank(type)) {
            handleNotify(extras);
        } else {
            Log.e(TAG, "unknown type: " + type);
        }
    }

    private void handleMessage(Bundle extras) {
        int notifyId = 1;
        try {
            if (extras.containsKey("notifyId")) {
                notifyId = Integer.valueOf(extras.getString("notifyId"));
            }
        } catch (Exception e) {
            Log.e(TAG, "invalid notify id: " + extras.getString("notifyId"));
        }

        Intent openIntent = new Intent(this, AndFHEMMainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openIntent.putExtra("unique", "foobar://" + SystemClock.elapsedRealtime());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notifyId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationUtil.notify(this, notifyId, pendingIntent, extras.getString("contentTitle"),
                extras.getString("contentText"), extras.getString("tickerText"),
                shouldVibrate(extras));
    }

    private void handleNotify(Bundle extras) {
        if (!extras.containsKey("changes")) return;

        String deviceName = extras.getString("deviceName");

        String changesText = extras.getString("changes");
        if (changesText == null) return;

        String[] changes = changesText.split("<\\|>");

        Map<String, String> changeMap = newHashMap();
        for (String change : changes) {
            String[] parts = change.split(":");
            if (parts.length != 2) continue;

            String key = parts[0].trim().toUpperCase(Locale.getDefault());
            String value = parts[1].trim();

            Tasker.sendTaskerNotifyIntent(this, deviceName, key, value);

            changeMap.put(key, value);
        }

        Intent parseIntent = new Intent(Actions.UPDATE_DEVICE_WITH_UPDATE_MAP);
        parseIntent.setClass(this, RoomListIntentService.class);
        parseIntent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
        parseIntent.putExtra(BundleExtraKeys.UPDATE_MAP, (Serializable) changeMap);
        parseIntent.putExtra(BundleExtraKeys.VIBRATE, shouldVibrate(extras));
        startService(parseIntent);
    }

    private boolean shouldVibrate(Bundle extras) {
        return extras.containsKey("vibrate") && "true".equalsIgnoreCase(extras.getString("vibrate"));
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        Log.i(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }

    @Override
    protected String[] getSenderIds(Context context) {
        ApplicationProperties applicationProperties = AndFHEMApplication.getApplication().getGraph().get(ApplicationProperties.class);
        String projectId = applicationProperties.getStringSharedPreference(GCM_PROJECT_ID, null);
        if (StringUtil.isBlank(projectId)) {
            return new String[]{};
        }
        return new String[]{projectId};
    }
}
