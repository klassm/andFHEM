package li.klass.fhem.activities.core;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.TimerTask;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;

public class UpdateTimerTask extends TimerTask {

    private final Context context;

    public UpdateTimerTask(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        Log.i(UpdateTimerTask.class.getName(), "send broadcast for device list update");

        Intent updateIntent = new Intent(Actions.DO_UPDATE);
        updateIntent.putExtra(BundleExtraKeys.DO_REFRESH, true);
        context.sendBroadcast(updateIntent);
    }
}
