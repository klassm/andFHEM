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

package li.klass.fhem.serv;

import android.content.Intent;
import android.os.ResultReceiver;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.RoomDeviceList;

import static li.klass.fhem.constants.Actions.FAVORITE_ADD;
import static li.klass.fhem.constants.Actions.FAVORITE_REMOVE;
import static li.klass.fhem.constants.Actions.FAVORITE_ROOM_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE;
import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;
import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;

public class FavoritesIntentService extends ConvenientIntentService {
    public FavoritesIntentService() {
        super(FavoritesIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        FavoritesSyncService service = FavoritesSyncService.INSTANCE;

        boolean doRefresh = intent.getBooleanExtra(DO_REFRESH, false);
        ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER);
        
        if (intent.getAction().equals(FAVORITE_ROOM_LIST)) {
            RoomDeviceList favorites = service.getFavorites(doRefresh);
            sendSingleExtraResult(resultReceiver, ResultCodes.SUCCESS, BundleExtraKeys.DEVICE_LIST, favorites);
        } else if (intent.getAction().equals(FAVORITE_ADD)) {
            Device device = (Device) intent.getSerializableExtra(DEVICE);
            service.addFavorite(device);
            if (resultReceiver != null) sendNoResult(resultReceiver, ResultCodes.SUCCESS);
        } else if (intent.getAction().equals(FAVORITE_REMOVE)) {
            Device device = (Device) intent.getSerializableExtra(DEVICE);
            service.removeFavorite(device);
            if (resultReceiver != null) sendNoResult(resultReceiver, ResultCodes.SUCCESS);
        }
    }
}
