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
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import li.klass.fhem.databinding.DeviceDetailCardPlayerBinding
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.devices.detail.ui.ExpandHandler
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.device.configuration.DeviceConfigurationProvider
import javax.inject.Inject

class PlayerCardProvider @Inject constructor(
        private val genericDeviceService: GenericDeviceService,
        private val deviceConfigurationProvider: DeviceConfigurationProvider
) : GenericDetailCardProvider {
    override fun ordering(): Int = 29

    override suspend fun provideCard(device: FhemDevice, context: Context, connectionId: String, navController: NavController, expandHandler: ExpandHandler): CardView? {
        val playerConfiguration =
            deviceConfigurationProvider.configurationFor(device).playerConfiguration
        if (playerConfiguration == null || playerConfiguration.hasAny()) {
            return null
        }

        val binding =
            DeviceDetailCardPlayerBinding.inflate(LayoutInflater.from(context), null, false)

        val provider = actionProviderFor(device, connectionId)

        attachActionTo(binding.rewind, provider(playerConfiguration.previousCommand))
        attachActionTo(binding.pause, provider(playerConfiguration.pauseCommand))
        attachActionTo(binding.stop, provider(playerConfiguration.stopCommand))
        attachActionTo(binding.play, provider(playerConfiguration.playCommand))
        attachActionTo(binding.forward, provider(playerConfiguration.nextCommand))

        return binding.root
    }

    private fun actionFor(command: String?, device: FhemDevice, connectionId: String?): View.OnClickListener? {
        command ?: return null
        return View.OnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    genericDeviceService.setState(device.xmlListDevice, command, connectionId)
                }
            }
        }
    }

    private fun actionProviderFor(device: FhemDevice, connectionId: String?): (String?) -> View.OnClickListener? {
        return { command: String? ->
            command?.let { actionFor(command, device, connectionId) }
        }
    }

    private fun attachActionTo(button: ImageButton, clickListener: View.OnClickListener?) {
        if (clickListener == null) {
            button.visibility = View.GONE
        } else {
            button.setOnClickListener(clickListener)
        }
    }
}