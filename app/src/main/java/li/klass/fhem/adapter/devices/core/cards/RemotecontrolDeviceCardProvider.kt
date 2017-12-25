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

package li.klass.fhem.adapter.devices.core.cards

import android.content.Context
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.google.common.base.Optional
import kotlinx.android.synthetic.main.remote_control_layout.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.GlideApp
import li.klass.fhem.R
import li.klass.fhem.connection.backend.DataConnectionSwitch
import li.klass.fhem.connection.backend.FHEMWEBConnection
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.devices.backend.RemotecontrolDeviceService
import li.klass.fhem.domain.GenericDevice
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.layoutInflater
import javax.inject.Inject

class RemotecontrolDeviceCardProvider @Inject constructor(
        private val remotecontrolDeviceService: RemotecontrolDeviceService,
        private val genericDeviceService: GenericDeviceService,
        private val dataConnectionSwitch: DataConnectionSwitch
) : GenericDetailCardProvider {
    override fun ordering(): Int = 29

    override fun provideCard(fhemDevice: GenericDevice, context: Context, connectionId: String?): CardView? {
        if (fhemDevice.xmlListDevice?.type != "remotecontrol") {
            return null
        }
        val view = context.layoutInflater.inflate(R.layout.remote_control_layout, null, false)
        val actionProvider = actionProviderFor(fhemDevice, connectionId, context)
        async(UI) {
            val rows = bg {
                remotecontrolDeviceService.getRowsFor(fhemDevice)
            }.await()
            updateTableWith(view.content, rows, context, actionProvider)
        }

        return view as CardView
    }

    private fun updateTableWith(table: TableLayout, rows: List<RemotecontrolDeviceService.Row>,
                                context: Context, actionProvider: (String?) -> View.OnClickListener?) {
        table.removeAllViews()
        rows.map { createTableRowForRemoteControlRow(it, context, actionProvider) }
                .forEach { table.addView(it) }
    }

    private fun createTableRowForRemoteControlRow(row: RemotecontrolDeviceService.Row,
                                                  context: Context, actionProvider: (String?) -> View.OnClickListener?): TableRow {
        return TableRow(context).apply {
            row.entries.forEach { entry ->
                addView(createImageViewFor(entry, this, context, actionProvider))
            }
        }
    }

    private fun createImageViewFor(entry: RemotecontrolDeviceService.Entry,
                                   tableRow: TableRow, context: Context, actionProvider: (String?) -> View.OnClickListener?): View {

        val imageButton = LayoutInflater.from(context).inflate(R.layout.remote_control_view, tableRow, false) as ImageButton

        val provider = dataConnectionSwitch.getProviderFor() as FHEMWEBConnection
        val authHeader = provider.basicAuthHeaders.authorization
        val glideUrl = GlideUrl(provider.server.url + entry.icon, LazyHeaders.Builder()
                .addHeader("Authorization", authHeader)
                .build())

        GlideApp.with(context)
                .load(glideUrl)
                .error(R.drawable.empty)
                .into(imageButton)

        imageButton.setOnClickListener(actionProvider(entry.command))

        return imageButton
    }

    private fun actionFor(command: String?, device: GenericDevice, connectionId: String?, context: Context): View.OnClickListener? {
        command ?: return null
        return View.OnClickListener {
            async(UI) {
                bg {
                    genericDeviceService.setState(device, command, Optional.fromNullable(connectionId))
                }
            }
        }
    }

    private fun actionProviderFor(device: GenericDevice, connectionId: String?, context: Context): (String?) -> View.OnClickListener? {
        return { command: String? ->
            command?.let { actionFor(command, device, connectionId, context) }
        }
    }
}