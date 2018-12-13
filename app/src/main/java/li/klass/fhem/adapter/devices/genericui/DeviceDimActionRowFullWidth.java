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

package li.klass.fhem.adapter.devices.genericui;

import android.content.Context;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import li.klass.fhem.update.backend.xmllist.XmlListDevice;

public abstract class DeviceDimActionRowFullWidth extends SeekBarActionRowFullWidth {

    public DeviceDimActionRowFullWidth(double dimState, double lowerBound, double dimStep, double upperBound, TableRow updateRow, int layoutId) {
        super(
                dimState,
                lowerBound,
                dimStep,
                upperBound,
                layoutId,
                updateRow);
    }

    public void onProgressChange(@NonNull final Context context, XmlListDevice device, double progress) {
        onStopDim(context, device, progress);
    }

    @NonNull
    @Override
    public String toUpdateText(XmlListDevice device, double progress) {
        return toDimUpdateText(device, progress);
    }

    public abstract void onStopDim(Context context, XmlListDevice device, double progress);

    public abstract String toDimUpdateText(XmlListDevice device, double progress);
}
