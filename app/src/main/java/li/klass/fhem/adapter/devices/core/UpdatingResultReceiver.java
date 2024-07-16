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

package li.klass.fhem.adapter.devices.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.ResultReceiver;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.ResultCodes;

public class UpdatingResultReceiver extends ResultReceiver {

    private final Context context;

    @SuppressWarnings("unused")
    private static final Creator<UpdatingResultReceiver> CREATOR = new Creator<UpdatingResultReceiver>() {
        @Override
        public UpdatingResultReceiver createFromParcel(Parcel source) {
            return (UpdatingResultReceiver) ResultReceiver.CREATOR.createFromParcel(source);
        }

        @Override
        public UpdatingResultReceiver[] newArray(int size) {
            return new UpdatingResultReceiver[0];
        }
    };

    public UpdatingResultReceiver(Context context) {
        super(new Handler());
        this.context = context;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);

        if (resultCode == ResultCodes.SUCCESS) {
            Intent intent = new Intent(Actions.INSTANCE.getDO_UPDATE());
            intent.setPackage(context.getPackageName());
            context.sendBroadcast(intent);
        }
    }
}
