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

package li.klass.fhem.appwidget.ui.widget.activity

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation
import li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation.deviceStateForFHEM
import li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation.isValidAdditionalInformationValue
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.util.DialogUtil
import org.jetbrains.anko.coroutines.experimental.bg
import javax.inject.Inject

class TargetStateAdditionalInformationActivity : Activity() {
    @Inject
    lateinit var genericDeviceService: GenericDeviceService
    @Inject
    lateinit var deviceListService: DeviceListService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndFHEMApplication.application?.daggerComponent?.inject(this)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.target_state_additional_information_selection_view)

        val extras = intent.extras
        if (extras == null) {
            finish()
            return
        }

        val deviceName = extras.getString(BundleExtraKeys.DEVICE_NAME)
        val targetState = extras.getString(BundleExtraKeys.DEVICE_TARGET_STATE)
        val additionalInformationType = deviceStateForFHEM(targetState)

        val deviceNameView = findViewById<TextView>(R.id.deviceName)
        deviceNameView.text = deviceName

        val targetStateView = findViewById<TextView>(R.id.targetState)
        targetStateView.text = targetState

        val additionalInfoView = findViewById<EditText>(R.id.additionalInformation)

        val okButton = findViewById<Button>(R.id.okButton)
        okButton.setOnClickListener {
            val content = additionalInfoView.text.toString()
            if (handleAdditionalInformationValue(content, additionalInformationType,
                            targetState, deviceName)) {
                finish()
            }
        }

        val cancelButton = findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener { finish() }
    }

    private fun handleAdditionalInformationValue(additionalInformation: String,
                                                 specialDeviceState: DeviceStateRequiringAdditionalInformation,
                                                 state: String?, deviceName: String?): Boolean {

        if (isValidAdditionalInformationValue(additionalInformation, specialDeviceState)) {
            switchDeviceState("$state $additionalInformation", deviceName)
            return true
        } else {
            DialogUtil.showAlertDialog(this, R.string.error, R.string.invalidInput)
            return false
        }
    }

    private fun switchDeviceState(newState: String, deviceName: String?) {
        async(UI) {
            bg {
                if (deviceName != null) {
                    deviceListService.getDeviceForName(deviceName, null)?.xmlListDevice
                            ?.let {
                                genericDeviceService.setState(it, newState, connectionId = null)
                            }
                }
            }
        }
    }
}
