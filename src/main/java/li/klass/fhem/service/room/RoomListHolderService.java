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

import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.FHEMWEBDevice;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.preferences.SharedPreferencesService;

@Singleton
public class RoomListHolderService {

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    ConnectionService connectionService;

    @Inject
    SharedPreferencesService sharedPreferencesService;

    private Map<String, RoomListCache> cache = new HashMap<>();

    @Inject
    public RoomListHolderService() {
    }

    public synchronized boolean storeDeviceListMap(RoomDeviceList roomDeviceList, Optional<String> connectionId, Context context) {
        return getCacheFor(connectionId, context).storeDeviceListMap(roomDeviceList, context);
    }

    public synchronized void clearRoomDeviceList(Optional<String> connectionId, Context context) {
        getCacheFor(connectionId, context).clearRoomDeviceList(context);
    }

    public Optional<RoomDeviceList> getCachedRoomDeviceListMap(Optional<String> connectionId, Context context) {
        return getCacheFor(connectionId, context).getCachedRoomDeviceListMap(context);
    }

    private RoomListCache getCacheFor(Optional<String> connectionId, Context context) {
        connectionId = connectionService.exists(connectionId, context) ? connectionId : Optional.<String>absent();
        return getCacheForConnectionId(connectionId, context);
    }

    public long getLastUpdate(Optional<String> connectionId, Context context) {
        return getCacheFor(connectionId, context).getLastUpdate(context);
    }

    private RoomListCache getCacheForConnectionId(Optional<String> connectionId, Context context) {
        String id = connectionId.or(connectionService.getSelectedId(context));
        if (!cache.containsKey(id)) {
            cache.put(id, new RoomListCache(id, applicationProperties, connectionService, sharedPreferencesService));
        }
        return cache.get(id);
    }

    public FHEMWEBDevice findFHEMWEBDevice(RoomDeviceList roomDeviceList, Context context) {
        return new FHEMWebDeviceInRoomDeviceListSupplier(applicationProperties, connectionService, roomDeviceList, context).get();
    }
}
