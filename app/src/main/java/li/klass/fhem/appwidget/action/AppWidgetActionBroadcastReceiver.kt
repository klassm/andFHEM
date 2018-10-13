package li.klass.fhem.appwidget.action

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import li.klass.fhem.AndFHEMApplication
import javax.inject.Inject

class AppWidgetActionBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var appWidgetActionHandler: AppWidgetActionHandler

    @Inject
    lateinit var application: Application

    init {
        AndFHEMApplication.application?.daggerComponent?.inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        val extras = intent.extras ?: return
        val action = intent.action ?: return
        appWidgetActionHandler.handle(applicationContext, extras, action)
    }

    private val applicationContext: Context get() = application.applicationContext
}