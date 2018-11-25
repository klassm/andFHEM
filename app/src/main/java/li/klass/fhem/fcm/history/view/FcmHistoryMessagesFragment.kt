package li.klass.fhem.fcm.history.view

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.fcm_history_messages.view.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import li.klass.fhem.R
import li.klass.fhem.constants.Actions
import li.klass.fhem.dagger.ApplicationComponent
import org.joda.time.LocalDate

class FcmHistoryMessagesFragment : FcmHistoryBaseFragment<FcmMessagesAdapter>(R.layout.fcm_history_messages) {

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun getAdapter() = FcmMessagesAdapter(emptyList())

    override fun doUpdateView(localDate: LocalDate, view: View) {
        runBlocking {
            val messages = async {
                fcmHistoryService.getMessages(localDate)
            }.await()

            activity?.sendBroadcast(Intent(Actions.DISMISS_EXECUTING_DIALOG))
            showEmptyViewIfRequired(messages.isEmpty(), view.messages, view.fcm_no_messages)
            (view.messages.adapter as FcmMessagesAdapter).updateWith(messages)
        }
    }

    override fun getRecyclerViewFrom(view: View): RecyclerView = view.messages
}