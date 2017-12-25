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
import android.widget.Button
import com.google.common.base.Optional
import kotlinx.android.synthetic.main.device_detail_card_fs20_zdr_player.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.R
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.domain.core.FhemDevice
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.layoutInflater
import javax.inject.Inject

class FS20ZdrPlayerCardProvider @Inject constructor(
        private val genericDeviceService: GenericDeviceService
) : GenericDetailCardProvider {
    override fun ordering(): Int = 29

    override fun provideCard(device: FhemDevice, context: Context, connectionId: String?): CardView? {
        if (device.xmlListDevice.type != "fs20_zdr") {
            return null
        }
        val view = context.layoutInflater.inflate(R.layout.device_detail_card_fs20_zdr_player, null, false)

        val provider = actionProviderFor(device, connectionId)

        attachActionTo(view.button_vol_up, provider("volume_up"))
        attachActionTo(view.button_vol_down, provider("volume_down"))
        attachActionTo(view.button_left, provider("left"))
        attachActionTo(view.button_right, provider("right"))
        attachActionTo(view.button_slp, provider("sleep"))
        attachActionTo(view.button_ms, provider("ms"))
        attachActionTo(view.button_prog_1, provider("1"))
        attachActionTo(view.button_prog_2, provider("2"))
        attachActionTo(view.button_prog_3, provider("3"))
        attachActionTo(view.button_prog_4, provider("4"))
        attachActionTo(view.button_prog_5, provider("5"))
        attachActionTo(view.button_prog_6, provider("6"))
        attachActionTo(view.button_prog_7, provider("7"))
        attachActionTo(view.button_prog_8, provider("8"))

        return view as CardView
    }

    private fun actionFor(command: String?, device: FhemDevice, connectionId: String?): View.OnClickListener? {
        command ?: return null
        return View.OnClickListener {
            async(UI) {
                bg {
                    genericDeviceService.setState(device, command, Optional.fromNullable(connectionId))
                }
            }
        }
    }

    private fun actionProviderFor(device: FhemDevice, connectionId: String?): (String?) -> View.OnClickListener? {
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