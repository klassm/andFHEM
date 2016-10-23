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

package li.klass.fhem.service.room;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.FHEMWEBDevice;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.util.ApplicationProperties;

@Singleton
public class RoomListHolderService {

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    ConnectionService connectionService;

    private Map<String, RoomListCache> cache = new HashMap<>();

    @Inject
    public RoomListHolderService() {
    }

    public synchronized boolean storeDeviceListMap(RoomDeviceList roomDeviceList, Context context) {
        return getCacheForCurrentConnection(context).storeDeviceListMap(roomDeviceList, context);
    }

    public synchronized void clearRoomDeviceList(Context context) {
        getCacheForCurrentConnection(context).clearRoomDeviceList(context);
    }

    public RoomDeviceList getCachedRoomDeviceListMap(Context context) {
        return getCacheForCurrentConnection(context).getCachedRoomDeviceListMap();
    }

    public RoomListCache getCacheForCurrentConnection(Context context) {
        String selectedId = connectionService.getSelectedId(context);
        return getCacheForConnectionId(selectedId);

    }

    private RoomListCache getCacheForConnectionId(String selectedId) {
        if (cache.containsKey(selectedId)) {
            return cache.get(selectedId);
        }
        return cache.put(selectedId, new RoomListCache(selectedId, applicationProperties, connectionService));
    }

    public FHEMWEBDevice findFHEMWEBDevice(RoomDeviceList roomDeviceList, Context context) {
        return new FHEMWebDeviceInRoomDeviceListSupplier(applicationProperties, connectionService, roomDeviceList, context).get();
    }
}
