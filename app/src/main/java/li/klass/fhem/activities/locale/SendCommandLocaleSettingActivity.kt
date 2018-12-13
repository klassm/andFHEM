/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.activities.locale

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.activities.locale.LocaleIntentConstants.EXTRA_BUNDLE
import li.klass.fhem.activities.locale.LocaleIntentConstants.EXTRA_STRING_BLURB
import li.klass.fhem.adapter.ConnectionListAdapter
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.connection.backend.FHEMServerSpec
import li.klass.fhem.connection.backend.ServerType
import li.klass.fhem.constants.Actions.EXECUTE_COMMAND
import li.klass.fhem.constants.BundleExtraKeys.*
import org.slf4j.LoggerFactory
import javax.inject.Inject

class SendCommandLocaleSettingActivity : Activity() {
    private var selectedId: String? = CURRENT_CONNECTION_ID

    @Inject
    lateinit var connectionService: ConnectionService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as AndFHEMApplication).daggerComponent.inject(this)

        setContentView(R.layout.locale_send_command)

        val commandView = findViewById<EditText>(R.id.fhemCommand)

        val intent = intent
        val bundle = intent.getBundleExtra(EXTRA_BUNDLE)
        if (bundle != null && bundle.containsKey(COMMAND)) {
            if (bundle.containsKey(COMMAND)) {
                commandView.setText(bundle.getString(COMMAND))
            }
            if (bundle.containsKey(CONNECTION_ID)) {
                selectedId = bundle.getString(CONNECTION_ID)
            }
        }

        val spinner = findViewById<Spinner>(R.id.connectionListSpinner)
        val connectionListAdapter = ConnectionListAdapter(this)
        spinner.adapter = connectionListAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                selectedId = view.tag as String
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        GlobalScope.launch(Dispatchers.Main) {
            val connections = async(Dispatchers.IO) {
                connectionService.listAll()
            }.await()
            fillConnectionSpinner(connections, connectionListAdapter)
        }

        val saveButton = findViewById<Button>(R.id.save)
        saveButton.setOnClickListener {
            val resultIntent = Intent()

            val command = commandView.text.toString()

            resultIntent.putExtra(EXTRA_STRING_BLURB, command)

            val resultBundle = Bundle()
            resultBundle.putString(ACTION, EXECUTE_COMMAND)
            resultBundle.putString(COMMAND, command)

            if (selectedId != null && CURRENT_CONNECTION_ID != selectedId) {
                resultBundle.putString(CONNECTION_ID, selectedId)
            }

            resultIntent.putExtra(EXTRA_BUNDLE, resultBundle)

            if (TaskerPlugin.Setting.hostSupportsOnFireVariableReplacement(this@SendCommandLocaleSettingActivity)) {
                TaskerPlugin.Setting.setVariableReplaceKeys(resultBundle, arrayOf(COMMAND))
            }

            LOG.info("onCreate() - result: command={}, action={}", resultBundle.getString(COMMAND), resultBundle.getString(ACTION))

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun fillConnectionSpinner(connectionList: List<FHEMServerSpec>, connectionListAdapter: ConnectionListAdapter) {
        val nonDummyConnections = connectionList.filterNot { it.serverType == ServerType.DUMMY }
                .toMutableList()

        val current = FHEMServerSpec(ConnectionChangeLocaleSettingActivity.CURRENT_CONNECTION_ID, ServerType.DUMMY, resources.getString(R.string.connectionCurrent))
        nonDummyConnections.add(0, current)

        connectionListAdapter.updateData(nonDummyConnections, selectedId)
        selectConnection(connectionListAdapter)
    }


    private fun selectConnection(connectionListAdapter: ConnectionListAdapter) {
        val spinner = findViewById<Spinner>(R.id.connectionListSpinner)
        val data = connectionListAdapter.data
        for (i in data.indices) {
            val spec = data[i]
            if (spec.id == selectedId) {
                spinner.setSelection(i)
                break
            }
        }
    }

    companion object {
        val CURRENT_CONNECTION_ID = "current"
        private val LOG = LoggerFactory.getLogger(SendCommandLocaleSettingActivity::class.java)
    }
}
