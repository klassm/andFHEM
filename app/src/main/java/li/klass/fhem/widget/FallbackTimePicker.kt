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
package li.klass.fhem.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener
import li.klass.fhem.util.NumberUtil

@SuppressLint("NewApi")
class FallbackTimePicker : LinearLayout {
    private lateinit var hourPicker: NumberPicker
    private lateinit var minutePicker: NumberPicker
    private var listener: ((hours: Int, minutes: Int) -> Unit)? = null

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    @SuppressLint("NewApi")
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    private fun init(context: Context) {
        val valueChangeListener = OnValueChangeListener { _, _, _ ->
            listener?.let { it(hours, minutes) }
        }
        val layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setLayoutParams(layoutParams)

        val numberPickerLayoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
            leftMargin = 15
            rightMargin = 15
        }

        hourPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 23
            initPicker(this, valueChangeListener, numberPickerLayoutParams)
        }
        addView(hourPicker)

        minutePicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 59
            initPicker(this, valueChangeListener, numberPickerLayoutParams)
        }
        addView(minutePicker)
    }

    private fun initPicker(picker: NumberPicker, valueChangeListener: OnValueChangeListener, numberPickerLayoutParams: LayoutParams) {
        picker.setFormatter(TWO_DIGIT_FORMATTER)
        picker.layoutParams = numberPickerLayoutParams
        picker.setOnValueChangedListener(valueChangeListener)
    }

    var minutes: Int
        get() {
            return minutePicker.displayedValues[minutePicker.value].toInt()
        }
        set(minutes) {
            minutePicker.value = minutes
        }

    fun setMinutesDisplayedValues(values: List<Int>) {
        val toDisplay = values.map { it.toString() }.map { it.padStart(2, '0') }
                .toTypedArray()
        minutePicker.minValue = 0
        minutePicker.maxValue = toDisplay.size - 1
        minutePicker.displayedValues = toDisplay
    }

    var hours: Int
        get() = hourPicker.value
        set(Hours) {
            hourPicker.value = Hours
        }

    fun setOnValueChangedListener(listener: (hours: Int, minutes: Int) -> Unit) {
        this.listener = listener
    }

    companion object {
        val TWO_DIGIT_FORMATTER = NumberPicker.Formatter { i -> NumberUtil.toTwoDecimalDigits(i) }
    }
}