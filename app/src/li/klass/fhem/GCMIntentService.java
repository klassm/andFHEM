package li.klass.fhem;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import li.klass.fhem.activities.AndFHEMMainActivity;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.PreferenceKeys;
import li.klass.fhem.service.room.DeviceListParser;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.StringUtil;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GCMIntentService extends GCMBaseIntentService {
    private static final String TAG = GCMIntentService.class.getName();

    @Override
    protected void onRegistered(Context context, String registrationId) {
        ApplicationProperties.INSTANCE.setSharedPreference(PreferenceKeys.GCM_REGISTRATION_ID, registrationId);
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
        if (!extras.containsKey("deviceName") || !extras.containsKey("changes") || !extras.containsKey("source")) {
            Log.i(TAG, "received GCM message, but doesn't fit required fields");
            return;
        }

        String deviceName = extras.getString("deviceName");
        String[] changes = extras.getString("changes").split("<\\|>");

        Map<String, String> changeMap = new HashMap<String, String>();
        for (String change : changes) {
            String[] parts = change.split(":");
            if (parts.length != 2) continue;

            changeMap.put(parts[0].trim().toUpperCase(), parts[1].trim());
        }

        Intent parseIntent = new Intent(Actions.UPDATE_DEVICE_WITH_UPDATE_MAP);
        parseIntent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
        parseIntent.putExtra(BundleExtraKeys.UPDATE_MAP, (Serializable) changeMap);
        startService(parseIntent);
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
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }

    public static void registerWithGCM(Context context) {
        String projectId = ApplicationProperties.INSTANCE.getStringSharedPreference(PreferenceKeys.GCM_PROJECT_ID, null);
        registerWithGCMInternal(context, projectId);
    }

    public static void registerWithGCM(Context context, String projectId) {
        GCMRegistrar.unregister(context);
        registerWithGCMInternal(context, projectId);
    }

    private static void registerWithGCMInternal(Context context, String projectId) {
        if (StringUtil.isBlank(projectId)) return;

        if (!GCMRegistrar.isRegistered(context)) {
            GCMRegistrar.checkDevice(context);
            GCMRegistrar.checkManifest(context);

            GCMRegistrar.setRegisterOnServerLifespan(context, 1000L * 60 * 60 * 24 * 30);
            GCMRegistrar.register(context, projectId);
        }
    }

    @Override
    protected String[] getSenderIds(Context context) {
        String projectId = ApplicationProperties.INSTANCE.getStringSharedPreference(PreferenceKeys.GCM_PROJECT_ID, null);
        if (StringUtil.isBlank(projectId)) {
            return new String[]{};
        }
        return new String[]{projectId};
    }
}
