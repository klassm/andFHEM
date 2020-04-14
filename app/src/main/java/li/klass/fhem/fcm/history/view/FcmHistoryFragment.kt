package li.klass.fhem.fcm.history.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fcm_history.view.*
import li.klass.fhem.R
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.fragments.core.BaseFragment
import javax.inject.Inject

class FcmHistoryFragment @Inject constructor(
        private val fcmHistoryMessagesFragment: FcmHistoryMessagesFragment,
        private val fcmHistoryUpdatesFragment: FcmHistoryUpdatesFragment
) : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fcm_history, container, false)
        val myActivity = activity ?: return null

        view?.viewpager?.adapter = FcmFragmentPagerAdapter(myActivity, childFragmentManager,
                fcmHistoryMessagesFragment, fcmHistoryUpdatesFragment)
        view?.tabs?.setupWithViewPager(view.viewpager)

        return view
    }

    override fun inject(applicationComponent: ApplicationComponent) {
    }

    override suspend fun update(refresh: Boolean) {
    }

    override fun mayPullToRefresh() = false
}