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

package li.klass.fhem.service.intent

import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import com.google.common.base.Strings
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.constants.ResultCodes
import li.klass.fhem.constants.ResultCodes.SUCCESS
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.fhem.connection.ServerType
import li.klass.fhem.service.connection.ConnectionService
import li.klass.fhem.service.room.RoomListService
import javax.inject.Inject

class ConnectionsIntentService : ConvenientIntentService(ConnectionsIntentService::class.java.name) {

    @Inject
    lateinit var connectionService: ConnectionService
    @Inject
    lateinit var roomListService: RoomListService

    override fun handleIntent(intent: Intent, updatePeriod: Long, resultReceiver: ResultReceiver?): ConvenientIntentService.State {
        val action = intent.action

        if (Actions.CONNECTIONS_LIST == action) {
            val serverSpecs = connectionService.listAll(this)

            val bundle = Bundle()
            bundle.putSerializable(CONNECTION_LIST, serverSpecs)
            bundle.putString(CONNECTION_ID, connectionService.getSelectedId(this))
            sendResult(resultReceiver, SUCCESS, bundle)
        } else if (Actions.CONNECTION_CREATE == action || Actions.CONNECTION_UPDATE == action) {

            val id = intent.getStringExtra(CONNECTION_ID)
            val name = intent.getStringExtra(CONNECTION_NAME)
            val serverType = ServerType.valueOf(intent.getStringExtra(CONNECTION_TYPE))
            val url = intent.getStringExtra(CONNECTION_URL)
            val alternateUrl = intent.getStringExtra(CONNECTION_ALTERNATE_URL)
            val username = intent.getStringExtra(CONNECTION_USERNAME)
            val password = intent.getStringExtra(CONNECTION_PASSWORD)
            val ip = intent.getStringExtra(CONNECTION_IP)
            val clientCertificatePath = intent.getStringExtra(CONNECTION_CLIENT_CERTIFICATE_PATH)
            val clientCertificatePassword = intent.getStringExtra(CONNECTION_CLIENT_CERTIFICATE_PASSWORD)

            var portString = intent.getStringExtra(CONNECTION_PORT)
            if (Strings.isNullOrEmpty(portString)) portString = "0"
            val port = Integer.valueOf(portString)!!

            if (Actions.CONNECTION_CREATE == action) {
                connectionService.create(name, serverType, username,
                        password, ip, port, url, alternateUrl, clientCertificatePath, clientCertificatePassword, this)
            } else {
                connectionService.update(id, name, serverType, username, password, ip,
                        port, url, alternateUrl, clientCertificatePath, clientCertificatePassword, this)
            }

            sendChangedBroadcast()

            return ConvenientIntentService.State.SUCCESS
        } else if (Actions.CONNECTION_GET == action) {
            val id = intent.getStringExtra(CONNECTION_ID)
            sendSingleExtraResult(resultReceiver, ResultCodes.SUCCESS, CONNECTION,
                    connectionService.forId(id, this))
        } else if (Actions.CONNECTION_DELETE == action) {
            val id = intent.getStringExtra(CONNECTION_ID)
            connectionService.delete(id, this)

            sendChangedBroadcast()
            return ConvenientIntentService.State.SUCCESS
        } else if (Actions.CONNECTION_SET_SELECTED == action) {
            val currentlySelected = connectionService.getSelectedId(this)
            val id = intent.getStringExtra(CONNECTION_ID)

            if (currentlySelected == id) return ConvenientIntentService.State.SUCCESS

            connectionService.setSelectedId(id, this)

            val updateIntent = Intent(Actions.DO_UPDATE)
            updateIntent.putExtra(BundleExtraKeys.DO_REFRESH, true)
            sendBroadcast(updateIntent)

            return ConvenientIntentService.State.SUCCESS
        } else if (Actions.CONNECTION_GET_SELECTED == action) {
            val bundle = Bundle()
            val selectedId = connectionService.getSelectedId(this)
            bundle.putString(CONNECTION_ID, selectedId)
            bundle.putSerializable(CONNECTION, connectionService.forId(selectedId, this))

            sendResult(resultReceiver, SUCCESS, bundle)
        }
        return ConvenientIntentService.State.DONE
    }

    private fun sendChangedBroadcast() {
        val changedIntent = Intent(Actions.CONNECTIONS_CHANGED)
        sendBroadcast(changedIntent)
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }
}
