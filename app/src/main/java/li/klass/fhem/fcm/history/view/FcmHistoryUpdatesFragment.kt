package li.klass.fhem.fcm.history.view

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fcm_history_updates.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import li.klass.fhem.R
import li.klass.fhem.constants.Actions
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.fcm.history.data.FcmHistoryService
import org.joda.time.LocalDate
import javax.inject.Inject

class FcmHistoryUpdatesFragment @Inject constructor(
        private val fcmHistoryService: FcmHistoryService
) : FcmHistoryBaseFragment<FcmUpdatesAdapter>(R.layout.fcm_history_updates) {

    override fun getAdapter() = FcmUpdatesAdapter(emptyList())

    override suspend fun doUpdateView(localDate: LocalDate, view: View) {
        coroutineScope {
            val updates = withContext(Dispatchers.IO) {
                fcmHistoryService.getChanges(localDate)
            }

            activity?.sendBroadcast(Intent(Actions.DISMISS_EXECUTING_DIALOG))
            showEmptyViewIfRequired(updates.isEmpty(), view.updates, view.fcm_no_updates)
            (view.updates.adapter as FcmUpdatesAdapter).updateWith(updates)
        }
    }

    override fun inject(applicationComponent: ApplicationComponent) {
    }

    override fun getRecyclerViewFrom(view: View): RecyclerView = view.updates
}