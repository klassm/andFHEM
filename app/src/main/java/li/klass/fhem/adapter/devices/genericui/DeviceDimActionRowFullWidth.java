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

import li.klass.fhem.domain.core.Device;

public abstract class DeviceDimActionRowFullWidth<D extends Device<D>> extends SeekBarActionRowFullWidth<D> {

    private final int lowerBound;
    private final int dimStep;
    private final int upperBound;

    public DeviceDimActionRowFullWidth(int dimState, int lowerBound, int dimStep, int upperBound, TableRow updateRow, int layoutId) {
        super(
                toDimProgress(dimState, lowerBound, dimStep),
                0,
                toDimProgress(upperBound, lowerBound, dimStep),
                layoutId, updateRow);

        this.lowerBound = lowerBound;
        this.dimStep = dimStep;
        this.upperBound = upperBound;
    }

    public void onStopTrackingTouch(final Context context, D device, int progress) {
        int dimProgress = dimProgressToDimState(progress, lowerBound, dimStep);
        onStopDim(context, device, dimProgress);
    }

    @Override
    public String toUpdateText(D device, int progress) {
        int dimProgress = dimProgressToDimState(progress, lowerBound, dimStep);
        return toDimUpdateText(device, dimProgress);
    }

    public abstract void onStopDim(Context context, D device, int progress);

    public abstract String toDimUpdateText(D device, int progress);

    static int toDimProgress(int progress, int lowerBound, int step) {
        return (progress - lowerBound) / step;
    }

    static int dimProgressToDimState(int progress, int lowerBound, int step) {
        return progress * step + lowerBound;
    }
}
