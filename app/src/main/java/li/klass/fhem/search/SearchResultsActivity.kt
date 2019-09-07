package li.klass.fhem.search

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import li.klass.fhem.R
import li.klass.fhem.activities.AndFHEMMainActivity
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.ui.FragmentType


class SearchResultsActivity : AppCompatActivity() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            startActivity(Intent(context, AndFHEMMainActivity::class.java).putExtras(
                    intent.extras ?: Bundle()))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.action) {
            Intent.ACTION_SEARCH -> handleSearch()
            Intent.ACTION_VIEW -> handleView()
        }
    }

    private fun handleView() {
        val deviceName = intent.extras?.getString("query")
        val bundle = Bundle()
        bundle.putString(BundleExtraKeys.DEVICE_NAME, deviceName)
        bundle.putSerializable(BundleExtraKeys.FRAGMENT, FragmentType.DEVICE_DETAIL)
        startActivity(Intent(this, AndFHEMMainActivity::class.java).putExtras(bundle))
        finish()
    }

    private fun handleSearch() {
        setContentView(R.layout.search_results_main)

        val fragment = SearchResultsFragment()
        fragment.arguments = intent.extras

        setTitle(R.string.search_title)
        supportFragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit()
    }

    override fun onResume() {
        val filter = IntentFilter()
        filter.addAction(Actions.SHOW_FRAGMENT)
        registerReceiver(receiver, filter)
        super.onResume()
    }

    override fun onPause() {
        unregisterReceiver(receiver);
        super.onPause()
    }
}