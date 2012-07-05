package li.klass.fhem.service;

import android.content.Intent;
import android.os.Bundle;
import li.klass.fhem.AndFHEMApplication;

public class AbstractService {
    /**
     * Sends a broadcast message containing a specified action. Context is the application context.
     * @param action action to use for sending the broadcast intent.
     * @param bundle parameters to set
     */
    protected void sendBroadcastWithAction(String action, Bundle bundle) {
        if (bundle == null) bundle = new Bundle();

        Intent broadcastIntent = new Intent(action);
        broadcastIntent.putExtras(bundle);
        AndFHEMApplication.getContext().sendBroadcast(broadcastIntent);
    }
}
