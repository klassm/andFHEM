package li.klass.fhem.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;

public class AbstractService {
    protected void sendBroadcastWithAction(String action) {
        sendBroadcastWithAction(action, null);
    }

    /**
     * Sends a broadcast message containing a specified action. Context is the application context.
     *
     * @param action action to use for sending the broadcast intent.
     * @param bundle parameters to set
     */
    protected void sendBroadcastWithAction(String action, Bundle bundle) {
        if (bundle == null) bundle = new Bundle();

        Intent broadcastIntent = new Intent(action);
        broadcastIntent.putExtras(bundle);
        AndFHEMApplication.getContext().sendBroadcast(broadcastIntent);
    }

    protected void showToast(int stringId) {
        Intent intent = new Intent(Actions.SHOW_TOAST);
        intent.putExtra(BundleExtraKeys.TOAST_STRING_ID, stringId);
        getContext().sendBroadcast(intent);
    }

    protected Context getContext() {
        return AndFHEMApplication.getContext();
    }
}
