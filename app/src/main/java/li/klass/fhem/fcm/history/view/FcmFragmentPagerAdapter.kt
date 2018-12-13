package li.klass.fhem.fcm.history.view

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import li.klass.fhem.R

class FcmFragmentPagerAdapter(val context: Context, fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment = when (position) {
        0 -> FcmHistoryMessagesFragment()
        else -> FcmHistoryUpdatesFragment()
    }

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> context.getString(R.string.fcm_history_messages)
        else -> context.getString(R.string.fcm_history_updates)
    }

    override fun getCount(): Int = 2
}