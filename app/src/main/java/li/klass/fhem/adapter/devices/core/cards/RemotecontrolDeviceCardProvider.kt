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
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import androidx.navigation.NavController
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import li.klass.fhem.GlideApp
import li.klass.fhem.R
import li.klass.fhem.connection.backend.DataConnectionSwitch
import li.klass.fhem.connection.backend.FHEMWEBConnection
import li.klass.fhem.databinding.RemoteControlLayoutBinding
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.devices.backend.RemotecontrolDeviceService
import li.klass.fhem.devices.detail.ui.ExpandHandler
import li.klass.fhem.domain.core.FhemDevice
import javax.inject.Inject

class RemotecontrolDeviceCardProvider @Inject constructor(
        private val remotecontrolDeviceService: RemotecontrolDeviceService,
        private val genericDeviceService: GenericDeviceService,
        private val dataConnectionSwitch: DataConnectionSwitch
) : GenericDetailCardProvider {
    override fun ordering(): Int = 29

    override suspend fun provideCard(device: FhemDevice, context: Context, connectionId: String, navController: NavController, expandHandler: ExpandHandler): androidx.cardview.widget.CardView? {
        if (device.xmlListDevice.type != "remotecontrol") {
            return null
        }
        val binding = RemoteControlLayoutBinding.inflate(LayoutInflater.from(context), null, false)
        val actionProvider = actionProviderFor(device, connectionId)
        coroutineScope {
            val rows = withContext(Dispatchers.Default) {
                remotecontrolDeviceService.getRowsFor(device)
            }
            updateTableWith(binding.content, rows, context, actionProvider)
        }

        return binding.root
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
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.empty)
                .into(imageButton)

        imageButton.setOnClickListener(actionProvider(entry.command))

        return imageButton
    }

    private fun actionFor(command: String?, device: FhemDevice, connectionId: String?): View.OnClickListener? {
        command ?: return null
        return View.OnClickListener {
            GlobalScope.launch((Dispatchers.IO)) {
                genericDeviceService.setState(device.xmlListDevice, command, connectionId)
            }
        }
    }

    private fun actionProviderFor(device: FhemDevice, connectionId: String?): (String?) -> View.OnClickListener? {
        return { command: String? ->
            command?.let { actionFor(command, device, connectionId) }
        }
    }
}