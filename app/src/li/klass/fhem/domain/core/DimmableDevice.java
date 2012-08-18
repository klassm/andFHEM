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

package li.klass.fhem.domain.core;

public abstract class DimmableDevice<D extends Device<D>> extends ToggleableDevice<D> {
    public final int getDimLowerBound() {
        return 0;
    }

    public int getDimPosition() {
        int position = getPositionForDimState(getState());
        if (position == -1) {
            return 0;
        }
        return position;
    }

    public int getDimUpPosition() {
        int currentPosition = getDimPosition();
        if (currentPosition + 1 > getDimUpperBound()) {
            return getDimUpperBound();
        }
        return currentPosition + 1;
    }

    public int getDimDownPosition() {
        int currentPosition = getDimPosition();
        if (currentPosition - 1 < getDimLowerBound()) {
            return getDimLowerBound();
        }
        return currentPosition - 1;
    }

    public abstract int getDimUpperBound();

    public abstract String getDimStateForPosition(int position);

    public abstract int getPositionForDimState(String dimState);

    public abstract boolean supportsDim();
}
