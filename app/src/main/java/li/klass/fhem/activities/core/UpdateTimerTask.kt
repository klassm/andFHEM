package li.klass.fhem.activities.core

import android.content.Context
import android.content.Intent
import android.util.Log
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import java.util.*

class UpdateTimerTask(private val context: Context) : TimerTask() {
    override fun run() {
        Log.i(UpdateTimerTask::class.java.name, "send broadcast for device list update")
        val updateIntent = Intent(Actions.DO_UPDATE)
        updateIntent.putExtra(BundleExtraKeys.DO_REFRESH, true)
        updateIntent.setPackage(context.packageName)
        context.sendBroadcast(updateIntent)
    }
}