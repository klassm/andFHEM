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

package li.klass.fhem.activities.locale

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.locale_getstate_plugin.*
import li.klass.fhem.R
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.devices.ui.DeviceNameSelectionActivity
import li.klass.fhem.domain.core.FhemDevice

class ConditionQueryLocaleSettingActivity : Activity() {
    private var selectedDeviceName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.locale_getstate_plugin)

        set.setOnClickListener { startActivityForResult(Intent(this@ConditionQueryLocaleSettingActivity, DeviceNameSelectionActivity::class.java), 0) }
        attributeType.adapter = ArrayAdapter<AttributeType>(this, android.R.layout.simple_dropdown_item_1line, AttributeType.values())

        intent?.let { intent ->
            intent.getStringExtra(DEVICE_NAME)?.let { setDeviceName(it) }
            intent.getStringExtra(DEVICE_TARGET_STATE)?.let { attributeValue.setText(it) }
            intent.getStringExtra(ATTRIBUTE_NAME)?.let { attributeName.setText(it) }
            intent.getStringExtra(ATTRIBUTE_TYPE)?.let { attributeType.setSelection(AttributeType.positionFor(it)) }
        }

        save.setOnClickListener {
            val name = attributeName.text.toString()
            val value = attributeValue.text
            val type = AttributeType.atPosition(attributeType.selectedItemPosition).description

            setResult(Activity.RESULT_OK, Intent()
                    .putExtra(DEVICE_TARGET_STATE, value.toString())
                    .putExtra(DEVICE_NAME, selectedDeviceName)
                    .putExtra(ATTRIBUTE_NAME, name)
                    .putExtra(ATTRIBUTE_TYPE, type)
                    .putExtra(LocaleIntentConstants.EXTRA_STRING_BLURB, "$selectedDeviceName ($type $name=$value)"))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val name = data?.getSerializableExtra(DEVICE)?.let { it as FhemDevice }?.name
        name?.let { setDeviceName(it) }
    }

    private fun setDeviceName(toSet: String) {
        this.selectedDeviceName = toSet
        deviceName.text = toSet
    }
}
