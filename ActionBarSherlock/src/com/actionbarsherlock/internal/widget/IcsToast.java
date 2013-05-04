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

package com.actionbarsherlock.internal.widget;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.R;

public class IcsToast extends Toast {
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    private static final String TAG = "Toast";

    public static Toast makeText(Context context, CharSequence s, int duration) {
        if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
            return Toast.makeText(context, s, duration);
        }
        IcsToast toast = new IcsToast(context);
        toast.setDuration(duration);
        TextView view = new TextView(context);
        view.setText(s);
        // Original AOSP using reference on @android:color/bright_foreground_dark
        // bright_foreground_dark - reference on @android:color/background_light
        // background_light - 0xffffffff
        view.setTextColor(0xffffffff);
        view.setGravity(Gravity.CENTER);
        view.setBackgroundResource(R.drawable.abs__toast_frame);
        toast.setView(view);
        return toast;
    }

    public static Toast makeText(Context context, int resId, int duration) {
        return makeText(context, context.getResources().getString(resId), duration);
    }

    public IcsToast(Context context) {
        super(context);
    }

    @Override
    public void setText(CharSequence s) {
        if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
            super.setText(s);
            return;
        }
        if (getView() == null) {
            return;
        }
        try {
            ((TextView) getView()).setText(s);
        } catch (ClassCastException e) {
            Log.e(TAG, "This Toast was not created with IcsToast.makeText", e);
        }
    }
}
