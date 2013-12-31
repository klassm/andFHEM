package li.klass.fhem;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import li.klass.fhem.activities.AndFHEMMainActivity;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.NotificationUtil;
import li.klass.fhem.util.StringUtil;

import static li.klass.fhem.constants.PreferenceKeys.GCM_PROJECT_ID;
import static li.klass.fhem.constants.PreferenceKeys.GCM_REGISTRATION_ID;

public class GCMIntentService extends GCMBaseIntentService {
    private static final String TAG = GCMIntentService.class.getName();

    @Override
    protected void onRegistered(Context context, String registrationId) {
        ApplicationProperties.INSTANCE.setSharedPreference(GCM_REGISTRATION_ID, registrationId);
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

    private boolean shouldVibrate(Bundle extras) {
        return extras.containsKey("vibrate") && "true".equalsIgnoreCase(extras.getString("vibrate"));
    }

    private void handleNotify(Bundle extras) {
        if (! extras.containsKey("changes")) return;

        String deviceName = extras.getString("deviceName");

        String changesText = extras.getString("changes");
        if (changesText == null) return;

        String[] changes = changesText.split("<\\|>");

        Map<String, String> changeMap = new HashMap<String, String>();
        for (String change : changes) {
            String[] parts = change.split(":");
            if (parts.length != 2) continue;

            changeMap.put(parts[0].trim().toUpperCase(), parts[1].trim());
        }

        Intent parseIntent = new Intent(Actions.UPDATE_DEVICE_WITH_UPDATE_MAP);
        parseIntent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
        parseIntent.putExtra(BundleExtraKeys.UPDATE_MAP, (Serializable) changeMap);
        parseIntent.putExtra(BundleExtraKeys.VIBRATE, shouldVibrate(extras));
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
        ApplicationProperties properties = ApplicationProperties.INSTANCE;
        String projectId = properties.getStringSharedPreference(GCM_PROJECT_ID, null);
        registerWithGCMInternal(context, projectId);
    }

    public static void registerWithGCM(Context context, String projectId) {
        ApplicationProperties.INSTANCE.setSharedPreference(GCM_REGISTRATION_ID, null);
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
        String projectId = ApplicationProperties.INSTANCE.getStringSharedPreference(GCM_PROJECT_ID, null);
        if (StringUtil.isBlank(projectId)) {
            return new String[]{};
        }
        return new String[]{projectId};
    }
}
