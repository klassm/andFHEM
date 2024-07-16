package li.klass.fhem.fcm.history.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import li.klass.fhem.constants.Actions
import li.klass.fhem.databinding.FcmHistoryMessagesBinding
import li.klass.fhem.fcm.history.data.FcmHistoryService
import org.joda.time.LocalDate
import javax.inject.Inject

class FcmHistoryMessagesFragment @Inject constructor(
    private val fcmHistoryService: FcmHistoryService
) : FcmHistoryBaseFragment<FcmMessagesAdapter>() {

    private lateinit var binding: FcmHistoryMessagesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val superView = super.onCreateView(inflater, container, savedInstanceState)
        if (superView != null) {
            return superView
        }
        binding = FcmHistoryMessagesBinding.inflate(inflater, container, false)
        fillView()
        return binding.root
    }

    override val selectedDateTextView: TextView
        get() = binding.selectedDate
    override val changeDateButton: ImageButton
        get() = binding.changeDateButton
    override val recyclerView: RecyclerView
        get() = binding.messages

    override fun getAdapter() = FcmMessagesAdapter(emptyList())

    override suspend fun doUpdateView(localDate: LocalDate, view: View) {
        coroutineScope {
            val messages = withContext(Dispatchers.IO) {
                fcmHistoryService.getMessages(localDate)
            }

            activity?.sendBroadcast(Intent(Actions.DISMISS_EXECUTING_DIALOG).apply { setPackage(context?.packageName) })
            showEmptyViewIfRequired(messages.isEmpty(), binding.messages, binding.fcmNoMessages)
            (binding.messages.adapter as FcmMessagesAdapter).updateWith(messages)
        }
    }
}