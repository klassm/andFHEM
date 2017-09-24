package li.klass.fhem.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import li.klass.fhem.R
import li.klass.fhem.constants.Actions
import li.klass.fhem.fragments.SearchResultsFragment


class SearchResultsActivity : AppCompatActivity() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            startActivity(Intent(context, AndFHEMMainActivity::class.java).putExtras(intent.extras))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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