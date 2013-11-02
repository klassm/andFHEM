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

package li.klass.fhem.service.intent;

import android.content.Intent;
import android.os.ResultReceiver;

import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.room.FavoritesService;

import static li.klass.fhem.constants.Actions.FAVORITES_PRESENT;
import static li.klass.fhem.constants.Actions.FAVORITE_ADD;
import static li.klass.fhem.constants.Actions.FAVORITE_REMOVE;
import static li.klass.fhem.constants.Actions.FAVORITE_ROOM_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.HAS_FAVORITES;
import static li.klass.fhem.constants.ResultCodes.SUCCESS;

public class FavoritesIntentService extends ConvenientIntentService {
    public FavoritesIntentService() {
        super(FavoritesIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        FavoritesService service = FavoritesService.INSTANCE;

        String action = intent.getAction();
        if (action == null) {
            return STATE.ERROR;
        }

        if (action.equals(FAVORITE_ROOM_LIST)) {
            RoomDeviceList favorites = service.getFavorites(updatePeriod);
            sendSingleExtraResult(resultReceiver, SUCCESS, DEVICE_LIST, favorites);
        } else if (action.equals(FAVORITE_ADD)) {
            Device device = (Device) intent.getSerializableExtra(DEVICE);
            service.addFavorite(device);
            if (resultReceiver != null) sendNoResult(resultReceiver, SUCCESS);
        } else if (action.equals(FAVORITE_REMOVE)) {
            Device device = (Device) intent.getSerializableExtra(DEVICE);
            service.removeFavorite(device);
            if (resultReceiver != null) sendNoResult(resultReceiver, SUCCESS);
        } else if (action.equals(FAVORITES_PRESENT)) {
            boolean hasFavorites = service.hasFavorites();
            sendSingleExtraResult(resultReceiver, SUCCESS, HAS_FAVORITES, hasFavorites);
        }

        return STATE.DONE;
    }
}
