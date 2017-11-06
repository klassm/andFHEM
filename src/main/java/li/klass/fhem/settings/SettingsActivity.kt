package li.klass.fhem.settings

import android.app.Activity
import android.os.Bundle
import li.klass.fhem.AndFHEMApplication

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as AndFHEMApplication).daggerComponent.inject(this)

        // Display the fragment as the main content.
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }
}