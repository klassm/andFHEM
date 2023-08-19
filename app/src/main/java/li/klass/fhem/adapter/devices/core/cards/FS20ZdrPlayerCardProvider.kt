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
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import li.klass.fhem.databinding.DeviceDetailCardFs20ZdrPlayerBinding
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.devices.detail.ui.ExpandHandler
import li.klass.fhem.domain.core.FhemDevice
import javax.inject.Inject

class FS20ZdrPlayerCardProvider @Inject constructor(
        private val genericDeviceService: GenericDeviceService
) : GenericDetailCardProvider {
    override fun ordering(): Int = 29

    override suspend fun provideCard(device: FhemDevice, context: Context, connectionId: String, navController: NavController, expandHandler: ExpandHandler): CardView? {
        if (device.xmlListDevice.type != "fs20_zdr") {
            return null
        }
        val binding =
            DeviceDetailCardFs20ZdrPlayerBinding.inflate(LayoutInflater.from(context), null, false)

        val provider = actionProviderFor(device, connectionId)

        attachActionTo(binding.buttonVolUp, provider("volume_up"))
        attachActionTo(binding.buttonVolDown, provider("volume_down"))
        attachActionTo(binding.buttonLeft, provider("left"))
        attachActionTo(binding.buttonRight, provider("right"))
        attachActionTo(binding.buttonSlp, provider("sleep"))
        attachActionTo(binding.buttonMs, provider("ms"))
        attachActionTo(binding.buttonProg1, provider("1"))
        attachActionTo(binding.buttonProg2, provider("2"))
        attachActionTo(binding.buttonProg3, provider("3"))
        attachActionTo(binding.buttonProg4, provider("4"))
        attachActionTo(binding.buttonProg5, provider("5"))
        attachActionTo(binding.buttonProg6, provider("6"))
        attachActionTo(binding.buttonProg7, provider("7"))
        attachActionTo(binding.buttonProg8, provider("8"))

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

    private fun actionProviderFor(device: FhemDevice, connectionId: String?): suspend (String?) -> View.OnClickListener? {
        return { command: String? ->
            command?.let { actionFor(command, device, connectionId) }
        }
    }

    private fun attachActionTo(button: Button, clickListener: View.OnClickListener?) {
        if (clickListener == null) {
            button.visibility = View.GONE
        } else {
            button.setOnClickListener(clickListener)
        }
    }
}