package li.klass.fhem.fcm.history.view

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fcm_history_messages.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import li.klass.fhem.R
import li.klass.fhem.constants.Actions
import li.klass.fhem.dagger.ApplicationComponent
import org.joda.time.LocalDate

class FcmHistoryMessagesFragment : FcmHistoryBaseFragment<FcmMessagesAdapter>(R.layout.fcm_history_messages) {

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun getAdapter() = FcmMessagesAdapter(emptyList())

    override suspend fun doUpdateView(localDate: LocalDate, view: View) {
        coroutineScope {
            val messages = withContext(Dispatchers.IO) {
                fcmHistoryService.getMessages(localDate)
            }

            activity?.sendBroadcast(Intent(Actions.DISMISS_EXECUTING_DIALOG))
            showEmptyViewIfRequired(messages.isEmpty(), view.messages, view.fcm_no_messages)
            (view.messages.adapter as FcmMessagesAdapter).updateWith(messages)
        }
    }

    override fun getRecyclerViewFrom(view: View): RecyclerView = view.messages
}