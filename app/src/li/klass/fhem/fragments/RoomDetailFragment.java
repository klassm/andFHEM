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
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.fragments;

import android.content.Intent;
import android.os.Bundle;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fragments.core.DeviceListFragment;

import java.io.Serializable;
import java.util.Map;

import static li.klass.fhem.constants.BundleExtraKeys.ROOM_NAME;

public class RoomDetailFragment extends DeviceListFragment {

    @SuppressWarnings("unused")
    public RoomDetailFragment(Bundle bundle) {
        super(bundle);
    }

    @SuppressWarnings("unused")
    public RoomDetailFragment() {
    }

    @Override
    protected void fillIntent(Intent intent) {
        super.fillIntent(intent);
        intent.putExtra(ROOM_NAME, creationAttributes.get(BundleExtraKeys.ROOM_NAME));
    }

    @Override
    protected String getUpdateAction() {
        return Actions.GET_ROOM_DEVICE_LIST;
    }

    @Override
    protected boolean onContentChanged(Map<String, Serializable> oldCreationAttributes, Map<String, Serializable> newCreationAttributes) {
        if (super.onContentChanged(oldCreationAttributes, newCreationAttributes)) return true;

        if (oldCreationAttributes != null && !oldCreationAttributes.get(BundleExtraKeys.ROOM_NAME)
                .equals(newCreationAttributes.get(BundleExtraKeys.ROOM_NAME))) {
            update(false);
            return true;
        }

        return false;
    }
}
