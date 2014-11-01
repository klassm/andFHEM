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

import javax.inject.Inject;

import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.room.FavoritesService;
import li.klass.fhem.service.room.RoomListService;

import static li.klass.fhem.constants.Actions.FAVORITES_IS_FAVORITES;
import static li.klass.fhem.constants.Actions.FAVORITES_PRESENT;
import static li.klass.fhem.constants.Actions.FAVORITE_ADD;
import static li.klass.fhem.constants.Actions.FAVORITE_REMOVE;
import static li.klass.fhem.constants.Actions.FAVORITE_ROOM_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.HAS_FAVORITES;
import static li.klass.fhem.constants.BundleExtraKeys.IS_FAVORITE;
import static li.klass.fhem.constants.ResultCodes.SUCCESS;

public class FavoritesIntentService extends ConvenientIntentService {
    @Inject
    FavoritesService favoritesService;

    @Inject
    RoomListService roomListService;

    public FavoritesIntentService() {
        super(FavoritesIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        String action = intent.getAction();
        if (action == null) {
            return STATE.ERROR;
        }

        if (roomListService.updateRoomDeviceListIfRequired(intent, updatePeriod) == RoomListService.RemoteUpdateRequired.REQUIRED) {
            return STATE.DONE;
        }

        if (FAVORITE_ROOM_LIST.equals(action)) {
            RoomDeviceList favorites = favoritesService.getFavorites();
            sendSingleExtraResult(resultReceiver, SUCCESS, DEVICE_LIST, favorites);
        } else if (FAVORITE_ADD.equals(action)) {
            Device device = (Device) intent.getSerializableExtra(DEVICE);
            favoritesService.addFavorite(device);
            if (resultReceiver != null) sendNoResult(resultReceiver, SUCCESS);
        } else if (FAVORITE_REMOVE.equals(action)) {
            Device device = (Device) intent.getSerializableExtra(DEVICE);
            favoritesService.removeFavorite(device);
            if (resultReceiver != null) sendNoResult(resultReceiver, SUCCESS);
        } else if (FAVORITES_PRESENT.equals(action)) {
            boolean hasFavorites = favoritesService.hasFavorites();
            sendSingleExtraResult(resultReceiver, SUCCESS, HAS_FAVORITES, hasFavorites);
        } else if (FAVORITES_IS_FAVORITES.equalsIgnoreCase(action)) {
            boolean isFavorite = favoritesService.isFavorite(intent.getStringExtra(BundleExtraKeys.DEVICE_NAME));
            sendSingleExtraResult(resultReceiver, SUCCESS, IS_FAVORITE, isFavorite);
        }

        return STATE.DONE;
    }
}
