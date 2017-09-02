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
import android.os.ResultReceiver
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.service.connection.ConnectionService
import javax.inject.Inject

class ConnectionsIntentService : ConvenientIntentService(ConnectionsIntentService::class.java.name) {

    @Inject
    lateinit var connectionService: ConnectionService

    override fun handleIntent(intent: Intent, updatePeriod: Long, resultReceiver: ResultReceiver?): ConvenientIntentService.State {
        val action = intent.action

        if (Actions.CONNECTION_SET_SELECTED == action) {
            val currentlySelected = connectionService.getSelectedId(this)
            val id = intent.getStringExtra(CONNECTION_ID)

            if (currentlySelected == id) return ConvenientIntentService.State.SUCCESS

            connectionService.setSelectedId(id, this)

            val updateIntent = Intent(Actions.DO_UPDATE)
            updateIntent.putExtra(BundleExtraKeys.DO_REFRESH, true)
            sendBroadcast(updateIntent)

            return ConvenientIntentService.State.SUCCESS
        }
        return ConvenientIntentService.State.DONE
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }
}
