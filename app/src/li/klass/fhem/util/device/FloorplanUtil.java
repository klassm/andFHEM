/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.util.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.ImageView;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;

public class FloorplanUtil {

    public static ImageView createSwitchStateBasedImageView(final Context context, final Device device) {
        ImageView buttonView = new ImageView(context);

        String state = device.getState();
        int drawable = R.drawable.toggle;
        if (state.equals("on")) {
            drawable = R.drawable.on;
        } else if (state.equals("off")) {
            drawable = R.drawable.off;
        } else if (state.startsWith("on-for-timer")) {
            drawable = R.drawable.on_for_timer;
        } else if (state.startsWith("off-for-timer")) {
            drawable = R.drawable.off_for_timer;
        }
        buttonView.setImageDrawable(context.getResources().getDrawable(drawable));

        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Actions.DEVICE_TOGGLE_STATE);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        context.sendBroadcast(new Intent(Actions.DO_UPDATE));
                    }
                });
                context.startService(intent);
            }
        });
        return buttonView;
    }
}
