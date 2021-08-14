package li.klass.fhem.fcm.history.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import li.klass.fhem.databinding.FcmHistoryBinding
import li.klass.fhem.fragments.core.BaseFragment
import javax.inject.Inject

class FcmHistoryFragment @Inject constructor(
        private val fcmHistoryMessagesFragment: FcmHistoryMessagesFragment,
        private val fcmHistoryUpdatesFragment: FcmHistoryUpdatesFragment
) : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewBinding = FcmHistoryBinding.inflate(inflater)
        val myActivity = activity ?: return null

        viewBinding.viewpager.adapter = FcmFragmentPagerAdapter(
            myActivity, childFragmentManager,
            fcmHistoryMessagesFragment, fcmHistoryUpdatesFragment
        )
        viewBinding.tabs.setupWithViewPager(viewBinding.viewpager)

        return viewBinding.root
    }

    override suspend fun update(refresh: Boolean) {
    }

    override fun mayPullToRefresh() = false
}