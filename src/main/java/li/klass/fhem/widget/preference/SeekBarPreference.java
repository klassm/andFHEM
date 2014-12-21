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

/* The following code was written by Matthew Wiggins
 * and is released under the APACHE 2.0 license 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package li.klass.fhem.widget.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import li.klass.fhem.R;

import static java.lang.Integer.parseInt;
import static li.klass.fhem.util.NumberUtil.isDecimalNumber;

/**
 * Preference showing a seek bar as dialog. The minimum, default and maximum values can
 * be configured using the respective getters and (partly) the xml configuration.
 * <p/>
 * The main layout is overtaken by
 * <a href="http://android.hlidskialf.com/blog/code/android-seekbar-preference">Hlidskialf Codes</a>.
 * However, the source code was heavily refactored and changed to fit the needs of andFHEM.
 * <p/>
 * As Android's seek bars always handle minimum values to be 0, we recalculate each value
 * to fit Android's needs. Each value is calculated to be <i>value - minimumValue</i>. That
 * way we can handle non 0 minimum values properly.
 * <p/>
 * This is also why internal values are stored in this recalculated format and not in the original
 * one provided by the using class. This concerns fields such as {@link #defaultValue},
 * {@link #maximumValue}, {@link #minimumValue} and {@link #internalValue}.
 */
public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {

    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    private Context context;

    /**
     * The seek bar users can use to change values.
     */
    private SeekBar seekBar;

    /**
     * A text field showing the current progress including a suffix value.
     */
    private TextView valueText;

    /**
     * Some message to show to the user. The message is shown above
     * the seek bar!
     */
    private String dialogMessageTop;

    /**
     * Text field for showing dialog messages,
     */
    private TextView dialogMessageTopTextView;


    /**
     * Some message to show to the user. The message is shown below
     * the seek bar.
     */
    private String dialogMessageBottom;

    /**
     * Text field for showing dialog messages below the text field,
     */
    private TextView dialogMessageBottomTextView;

    /**
     * A suffix shown after the value within the {@link #valueText} field.
     */
    private String suffix;

    /**
     * A default value which is used whenever no persisted value can be found.
     */
    private int defaultValue;

    /**
     * A maximum value.
     * Internal note: Make sure that a progress which is set to the seek bar is always within bounds.
     * If a value is bigger than the maximum value, the maximum value is used.
     */
    private int maximumValue;

    /**
     * A minimum value. This is used to recalculate all other values to the internal format.
     */
    private int minimumValue;

    /**
     * The current progress (internal format).
     */
    private int internalValue = 0;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        dialogMessageTop = attrs.getAttributeValue(ANDROID_NS, "dialogMessage");
        suffix = attrs.getAttributeValue(ANDROID_NS, "text");
        if (suffix.startsWith("@") && isDecimalNumber(suffix.substring(1))) {
            suffix = context.getString(parseInt(suffix.substring(1)));
        }

        setDefaultValue(attrs.getAttributeIntValue(ANDROID_NS, "defaultValue", 0));
        setMaximumValue(attrs.getAttributeIntValue(ANDROID_NS, "max", 100));
    }

    @Override
    public void setDefaultValue(Object newDefaultValue) {
        if (!(newDefaultValue instanceof Integer)) {
            return;
        }

        int newDefaultValueExternal = (Integer) newDefaultValue;
        int newDefaultValueInternal = toInternalValue(newDefaultValueExternal, minimumValue);

        // Overwrite the current default.
        if (defaultValue == internalValue) {
            setValue(newDefaultValueExternal);
        }

        this.defaultValue = newDefaultValueInternal;

        super.setDefaultValue(newDefaultValueInternal);
    }

    public void setMaximumValue(int maximumValue) {
        this.maximumValue = toInternalValue(maximumValue, minimumValue);
        if (internalValue > maximumValue) internalValue = maximumValue;

        if (seekBar != null) {
            seekBar.setMax(maximumValue);
        }
    }

    private static int toInternalValue(int value, int minimumValue) {
        return value - minimumValue;
    }

    @Override
    protected View onCreateDialogView() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.seekbar_preference_dialog, null);

        assert view != null;

        dialogMessageTopTextView = (TextView) view.findViewById(R.id.dialogMessageTop);
        setDialogMessageTop(dialogMessageTop);

        dialogMessageBottomTextView = (TextView) view.findViewById(R.id.dialogMessageBottom);
        setDialogMessageBottom(dialogMessageBottom);

        valueText = (TextView) view.findViewById(R.id.value);

        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(maximumValue);

        if (shouldPersist()) {
            int externalDefault = toExternalValue(defaultValue, minimumValue);
            int loadValue = getPersistedInt(externalDefault);
            setValue(loadValue);
        }

        return view;
    }

    public void setDialogMessageTop(String dialogMessageTop) {
        this.dialogMessageTop = dialogMessageTop;

        if (dialogMessageTop != null) {
            dialogMessageTopTextView.setText(dialogMessageTop);
        }
    }

    public void setDialogMessageBottom(String dialogMessageBottom) {
        this.dialogMessageBottom = dialogMessageBottom;

        if (dialogMessageBottom != null) {
            dialogMessageBottomTextView.setText(dialogMessageBottom);
        }
    }

    private static int toExternalValue(int value, int minimumValue) {
        return value + minimumValue;
    }

    @Override
    protected void onBindDialogView(@NotNull View v) {
        super.onBindDialogView(v);

        seekBar.setMax(maximumValue);
        seekBar.setProgress(internalValue);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object def) {
        super.onSetInitialValue(restore, defaultValue);

        if (restore) {
            internalValue = getPersistedInt(defaultValue);
        } else {
            internalValue = defaultValue;
        }

        internalValue -= minimumValue;
    }

    public void onProgressChanged(SeekBar seek, int newValue, boolean fromTouch) {
        this.internalValue = newValue;

        updateValueText();
    }

    /**
     * Update the {@link #valueText} field to match the current progress.
     */
    private void updateValueText() {
        int persistValue = getValue();

        String text = String.valueOf(persistValue);
        if (valueText != null) {
            valueText.setText(suffix == null ? text : text + " " + suffix);
        }

        callChangeListener(persistValue);
    }

    public int getValue() {
        return toExternalValue(internalValue, minimumValue);
    }

    public void setValue(int value) {
        internalValue = toInternalValue(value, minimumValue);

        if (seekBar != null) {
            seekBar.setProgress(internalValue);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (!positiveResult) return;

        if (shouldPersist()) {
            persistInt(toExternalValue(internalValue, minimumValue));
        }
    }

    public void onStartTrackingTouch(SeekBar seek) {
    }

    public void onStopTrackingTouch(SeekBar seek) {
    }

    /**
     * Sets the minimum value and recalculates all internal values to match the new minimum value.
     * (We do not want to loose state).
     *
     * @param newMinimumValue minimum to set.
     */
    public void setMinimumValue(int newMinimumValue) {
        int maximumValueExternal = toExternalValue(maximumValue, minimumValue);
        int defaultValueExternal = toExternalValue(defaultValue, minimumValue);
        int currentValue = toExternalValue(internalValue, minimumValue);

        this.minimumValue = newMinimumValue;

        setMaximumValue(maximumValueExternal);
        setDefaultValue(defaultValueExternal);
        setValue(currentValue);
    }
}