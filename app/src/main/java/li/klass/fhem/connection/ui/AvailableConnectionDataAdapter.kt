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

package li.klass.fhem.connection.ui

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import kotlinx.coroutines.*
import li.klass.fhem.R
import li.klass.fhem.adapter.ListDataAdapter
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.connection.backend.FHEMServerSpec
import li.klass.fhem.connection.backend.ServerType
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.databinding.ConnectionSpinnerItemBinding
import org.slf4j.LoggerFactory

class AvailableConnectionDataAdapter(
    private val parent: Spinner,
    private val onConnectionChanged: Runnable,
    private val connectionService: ConnectionService,
    private val onConnectionManagementSelected: () -> Unit
) : ListDataAdapter<FHEMServerSpec>(parent.context, ArrayList()),
    AdapterView.OnItemSelectedListener {
    private var currentlySelectedPosition = -1

    class ManagementPill :
        FHEMServerSpec(ConnectionService.MANAGEMENT_DATA_ID, ServerType.DUMMY, "") {
        override fun compareTo(other: FHEMServerSpec): Int = 1
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val server = getData()[position]

        val binding =
            if (convertView != null) ConnectionSpinnerItemBinding.bind(convertView) else ConnectionSpinnerItemBinding.inflate(
                inflater,
                parent,
                false
            )

        if (server is ManagementPill) {
            binding.name.setText(R.string.connectionManage)
            binding.type.visibility = View.GONE
        } else {
            binding.name.text = server.name
            binding.type.text = server.serverType.name
            binding.type.visibility = View.VISIBLE
        }

        return binding.root
    }

    suspend fun doLoad() {
        coroutineScope {
            val (all, selected) = withContext(Dispatchers.IO) {
                Pair(connectionService.listAll(), connectionService.getSelectedId())
            }

            updateData(all.toMutableList())
            select(selected)
        }
    }

    private fun select(id: String?) {
        val data = getData()
        for (i in data.indices) {
            LOG.trace("select(id=$id) - data[$i]=${data[i]}")
            if (data[i].id == id) {
                parent.setSelection(i)
            }
        }
    }

    override fun updateData(newData: MutableList<FHEMServerSpec>?) {
        newData?.add(MANAGEMENT_PILL)
        super.updateData(newData)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        if (connectionService.getSelectedId() == getData()[pos].id) {
            return
        }
        if (pos == getData().size - 1) {
            parent.setSelection(currentlySelectedPosition)
            onConnectionManagementSelected()
        } else if (currentlySelectedPosition != pos) {
            LOG.info("onItemSelected - changing from $currentlySelectedPosition to $pos")
            currentlySelectedPosition = pos
            val myContext = context
            GlobalScope.launch(Dispatchers.Main) {
                launch {
                    connectionService.setSelectedId(getData()[pos].id)
                    if (currentlySelectedPosition != -1) {
                        myContext.sendBroadcast(
                            Intent(Actions.DO_UPDATE).putExtra(
                                BundleExtraKeys.DO_REFRESH,
                                false
                            ).apply { setPackage(context.packageName) }
                        )
                    }
                }
            }
        }
        onConnectionChanged.run()
    }

    override fun onNothingSelected(adapterView: AdapterView<*>) {
        currentlySelectedPosition = -1
    }

    companion object {
        private val MANAGEMENT_PILL = ManagementPill()
        private val LOG = LoggerFactory.getLogger(AvailableConnectionDataAdapter::class.java)
    }
}
