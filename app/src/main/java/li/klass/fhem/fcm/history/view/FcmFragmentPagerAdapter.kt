package li.klass.fhem.fcm.history.view

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import li.klass.fhem.R

class FcmFragmentPagerAdapter(
        val context: Context, fragmentManager: FragmentManager,
        private val fcmHistoryMessagesFragment: FcmHistoryMessagesFragment,
        private val fcmHistoryUpdatesFragment: FcmHistoryUpdatesFragment
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment = when (position) {
        0 -> fcmHistoryMessagesFragment
        else -> fcmHistoryUpdatesFragment
    }

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> context.getString(R.string.fcm_history_messages)
        else -> context.getString(R.string.fcm_history_updates)
    }

    override fun getCount(): Int = 2
}