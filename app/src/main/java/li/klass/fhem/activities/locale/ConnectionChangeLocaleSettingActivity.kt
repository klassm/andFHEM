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
import android.widget.Spinner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.adapter.ConnectionListAdapter
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.connection.backend.FHEMServerSpec
import li.klass.fhem.connection.backend.ServerType
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.BundleExtraKeys.COMMAND
import li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID
import javax.inject.Inject

class ConnectionChangeLocaleSettingActivity : Activity() {
    private var selectedId: String? = CURRENT_CONNECTION_ID
    private var selectedName: String? = null

    @Inject
    lateinit var connectionService: ConnectionService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as AndFHEMApplication).daggerComponent.inject(this)

        setContentView(R.layout.locale_change_connection)

        val intent = intent
        val bundle = intent.getBundleExtra(LocaleIntentConstants.EXTRA_BUNDLE)
        if (bundle != null && bundle.containsKey(COMMAND)) {
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
                selectedName = connectionListAdapter.data[i].name
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

            val resultBundle = Bundle()
            resultBundle.putString(BundleExtraKeys.ACTION, Actions.CONNECTION_UPDATE)
            resultIntent.putExtra(LocaleIntentConstants.EXTRA_BUNDLE, resultBundle)

            if (selectedId != null && CURRENT_CONNECTION_ID != selectedId) {
                resultBundle.putString(CONNECTION_ID, selectedId)
            }

            if (selectedName == null) {
                selectedName = resources.getString(R.string.connectionCurrent)
            }
            resultIntent.putExtra(LocaleIntentConstants.EXTRA_STRING_BLURB, selectedName)

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun fillConnectionSpinner(connectionList: List<FHEMServerSpec>, connectionListAdapter: ConnectionListAdapter) {
        val nonDummyConnections = connectionList.filterNot { it.serverType == ServerType.DUMMY }
                .toMutableList()

        val current = FHEMServerSpec(CURRENT_CONNECTION_ID, ServerType.DUMMY, resources.getString(R.string.connectionCurrent))
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
                selectedName = spec.name
                break
            }
        }
    }

    companion object {
        val CURRENT_CONNECTION_ID = "current"
    }
}
