package li.klass.fhem.fcm.history.view

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.fcm_history_updates.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.R
import li.klass.fhem.constants.Actions
import li.klass.fhem.dagger.ApplicationComponent
import org.jetbrains.anko.coroutines.experimental.bg
import org.joda.time.LocalDate

class FcmHistoryUpdatesFragment : FcmHistoryBaseFragment<FcmUpdatesAdapter>(R.layout.fcm_history_updates) {

    override fun getAdapter() = FcmUpdatesAdapter(emptyList())

    override fun doUpdateView(localDate: LocalDate, view: View) {
        async(UI) {
            val updates = bg {
                fcmHistoryService.getChanges(localDate)
            }.await()

            activity.sendBroadcast(Intent(Actions.DISMISS_EXECUTING_DIALOG))
            showEmptyViewIfRequired(updates.isEmpty(), view.updates, view.fcm_no_updates)
            (view.updates.adapter as FcmUpdatesAdapter).updateWith(updates)
        }
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun getRecyclerViewFrom(view: View): RecyclerView = view.updates
}