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

package li.klass.fhem.widget.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

import li.klass.fhem.R;
import li.klass.fhem.service.CommandExecutionService;

public class NumberOfRetriesSeekBarPreference extends SeekBarPreference {
    public NumberOfRetriesSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onProgressChanged(SeekBar seek, int newValue, boolean fromTouch) {
        super.onProgressChanged(seek, newValue, fromTouch);

        setDialogMessageBottom(createText(newValue));
    }

    private String createText(int tries) {
        String text = "";

        Context context = getContext();
        String template = context.getString(R.string.prefCommandExecutionRetriesTimeDialogEntry);
        template += "\r\n";

        text += String.format(template, 0, 0);

        for (int i = 1; i <= tries; i++) {
            text += String.format(template, i, CommandExecutionService.secondsForTry(i));
        }

        return text;
    }
}
