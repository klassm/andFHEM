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

package li.klass.fhem.ui;

import android.content.Context;
import android.view.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import li.klass.fhem.R;
import li.klass.fhem.util.DialogUtil;

public class AndroidBug {
    private static final Logger LOG = LoggerFactory.getLogger(AndroidBug.class);

    /**
     * Android contains a bug causing an {@link java.lang.ArrayIndexOutOfBoundsException} when
     * inflating {@link android.widget.TimePicker}s.
     * <p/>
     * <code>
     * android.view.InflateException: Binary XML file line #47: Error inflating class android.widget.TimePicker
     * at android.view.LayoutInflater.createView(LayoutInflater.java:633)
     * ...
     * Caused by: java.lang.ArrayIndexOutOfBoundsException: length=1; index=1
     * at android.content.res.ColorStateList.addFirstIfMissing(ColorStateList.java:356)
     * </code>
     * <p/>
     * Current status:
     * Google has acknowledged the bug but has not yet released a fix (see <a href="https://code.google.com/p/android/issues/detail?id=78984">Google Code</a>)
     */
    public static View handleColorStateBug(BugHandler bugHandler) {
        try {
            return bugHandler.defaultAction();
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.error("color state bug encountered", e);
            return bugHandler.bugEncountered();
        }
    }

    public static void showMessageIfColorStateBugIsEncountered(final Context context, final MessageBugHandler messageBugHandler) {
        handleColorStateBug(new BugHandler() {
            @Override
            public View bugEncountered() {
                DialogUtil.showAlertDialog(context, R.string.androidBugDialogDatePickerTitle, R.string.androidBugDialogDatePickerContent);
                return null;
            }

            @Override
            public View defaultAction() {
                messageBugHandler.defaultAction();
                return null;
            }
        });
    }

    public interface BugHandler {
        View bugEncountered();

        View defaultAction();
    }

    public interface MessageBugHandler {
        void defaultAction();
    }
}
