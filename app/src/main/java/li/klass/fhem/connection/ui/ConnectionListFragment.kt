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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import kotlinx.android.synthetic.main.connection_list.view.*
import kotlinx.coroutines.*
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.adapter.ConnectionListAdapter
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.connection.backend.FHEMServerSpec
import li.klass.fhem.connection.backend.ServerType
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID
import li.klass.fhem.constants.ResultCodes
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.service.intent.LicenseIntentService
import li.klass.fhem.ui.FragmentType
import li.klass.fhem.util.FhemResultReceiver
import li.klass.fhem.util.Reject
import javax.inject.Inject

class ConnectionListFragment : BaseFragment() {

    @Inject
    lateinit var advertisementService: AdvertisementService
    @Inject
    lateinit var connectionService: ConnectionService

    private var clickedConnectionId: String? = null
    private var connectionId: String? = null

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        connectionId = args?.getString(CONNECTION_ID)
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val superView = super.onCreateView(inflater, container, savedInstanceState)
        if (superView != null) return superView
        val myActivity = activity ?: return superView

        val adapter = ConnectionListAdapter(activity)
        val layout = inflater.inflate(R.layout.connection_list, container, false)
        advertisementService.addAd(layout, myActivity)

        val emptyView = layout.findViewById<LinearLayout>(R.id.emptyView)
        fillEmptyView(emptyView)

        val connectionList = layout.findViewById<ListView>(R.id.connectionList)
        Reject.ifNull(connectionList)
        connectionList.adapter = adapter

        connectionList.onItemClickListener = AdapterView.OnItemClickListener { _, view, _, _ ->
            val connectionId = view.tag as String
            onClick(connectionId)
        }
        registerForContextMenu(connectionList)

        return layout
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.connections_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.connection_add) {
            val size = adapter.data.size

            activity?.startService(Intent(Actions.IS_PREMIUM)
                    .setClass(activity, LicenseIntentService::class.java)
                    .putExtra(BundleExtraKeys.RESULT_RECEIVER, object : FhemResultReceiver() {
                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            val isPremium = resultCode == ResultCodes.SUCCESS && resultData!!.getBoolean(BundleExtraKeys.IS_PREMIUM, false)

                            if (!isPremium && size >= AndFHEMApplication.PREMIUM_ALLOWED_FREE_CONNECTIONS) {
                                activity?.sendBroadcast(Intent(Actions.SHOW_ALERT)
                                        .putExtra(BundleExtraKeys.ALERT_CONTENT_ID, R.string.premium_multipleConnections)
                                        .putExtra(BundleExtraKeys.ALERT_TITLE_ID, R.string.premium))
                            } else {
                                activity?.sendBroadcast(Intent(Actions.SHOW_FRAGMENT)
                                        .putExtra(BundleExtraKeys.FRAGMENT, FragmentType.CONNECTION_DETAIL))
                            }
                        }
                    }))
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun getTitle(context: Context): CharSequence? =
            context.getString(R.string.connectionManageTitle)

    private fun fillEmptyView(view: LinearLayout) {
        val emptyView = LayoutInflater.from(activity).inflate(R.layout.empty_view, view)!!
        val emptyText = emptyView.findViewById<TextView>(R.id.emptyText)
        emptyText.setText(R.string.noConnections)
    }

    private fun onClick(connectionId: String) {
        activity?.sendBroadcast(Intent(Actions.SHOW_FRAGMENT)
                .putExtra(BundleExtraKeys.FRAGMENT, FragmentType.CONNECTION_DETAIL)
                .putExtra(CONNECTION_ID, connectionId))
    }

    private val adapter: ConnectionListAdapter
        get() {
            val listView = view!!.findViewById<ListView>(R.id.connectionList)
            return listView.adapter as ConnectionListAdapter
        }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val info = menuInfo as AdapterView.AdapterContextMenuInfo
        val tag = info.targetView.tag

        if (tag == null || tag !is String) return

        clickedConnectionId = tag

        menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.context_delete)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        super.onContextItemSelected(item)

        if (clickedConnectionId == null) return false
        when (item!!.itemId) {
            CONTEXT_MENU_DELETE -> {
                GlobalScope.launch(Dispatchers.Main) {
                    async(Dispatchers.IO) {
                        connectionService.delete(clickedConnectionId!!)
                    }.await()
                    updateAsync(false)
                }
                return true
            }
        }
        return false
    }

    override suspend fun update(refresh: Boolean) {
        if (view == null) return

        hideEmptyView()
        val myActivity = activity ?: return
        if (refresh) myActivity.sendBroadcast(Intent(Actions.SHOW_EXECUTING_DIALOG))

        doUpdate()
    }

    private suspend fun doUpdate() {
        coroutineScope {
            val connectionList = async(Dispatchers.IO) {
                connectionService.listAll()
            }.await()

            val nonDummyConnections = connectionList.filterNot { it.serverType == ServerType.DUMMY }
            view!!.emptyView.visibility = if (nonDummyConnections.isEmpty()) View.VISIBLE else View.GONE

            adapter.updateData(nonDummyConnections, connectionId)
            scrollToSelected(connectionId, adapter.data)
        }
    }

    private fun scrollToSelected(selectedConnectionId: String?, serverList: List<FHEMServerSpec>) {
        if (selectedConnectionId == null) return

        val view = view ?: return

        val connectionListView = view.findViewById<ListView>(R.id.connectionList)

        for (i in serverList.indices) {
            val serverSpec = serverList[i]
            if (serverSpec.id == selectedConnectionId) {
                connectionListView.setSelection(i)
                return
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        activity?.sendBroadcast(Intent(Actions.CONNECTIONS_CHANGED))
    }

    companion object {
        private val CONTEXT_MENU_DELETE = 1
    }
}
