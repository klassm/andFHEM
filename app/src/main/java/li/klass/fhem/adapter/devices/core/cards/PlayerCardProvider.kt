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
import android.view.View
import android.widget.ImageButton
import com.google.common.base.Optional
import kotlinx.android.synthetic.main.device_detail_card_player.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.R
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.domain.GenericDevice
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.layoutInflater
import javax.inject.Inject

class PlayerCardProvider @Inject constructor(
        private val genericDeviceService: GenericDeviceService
) : GenericDetailCardProvider {
    override fun ordering(): Int = 29

    override fun provideCard(fhemDevice: GenericDevice, context: Context, connectionId: String?): CardView? {
        val playerConfiguration = fhemDevice.deviceConfiguration.playerConfiguration
        if (playerConfiguration == null || !playerConfiguration.hasAny()) {
            return null
        }

        val view = context.layoutInflater.inflate(R.layout.device_detail_card_player, null, false)

        val provider = actionProviderFor(fhemDevice, connectionId, context)

        attachActionTo(view.rewind, provider(playerConfiguration.previousCommand))
        attachActionTo(view.pause, provider(playerConfiguration.pauseCommand))
        attachActionTo(view.stop, provider(playerConfiguration.stopCommand))
        attachActionTo(view.play, provider(playerConfiguration.playCommand))
        attachActionTo(view.forward, provider(playerConfiguration.nextCommand))

        return view as CardView
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

    private fun attachActionTo(button: ImageButton, clickListener: View.OnClickListener?) {
        if (clickListener == null) {
            button.visibility = View.GONE
        } else {
            button.setOnClickListener(clickListener)
        }
    }
}