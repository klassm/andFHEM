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

package li.klass.fhem.dagger;

import dagger.Module;
import li.klass.fhem.billing.BillingService;
import li.klass.fhem.fhem.DataConnectionSwitch;
import li.klass.fhem.gcm.GCMIntentService;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.DateService;
import li.klass.fhem.service.SharedPreferencesService;
import li.klass.fhem.service.advertisement.AdvertisementService;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.service.device.AtService;
import li.klass.fhem.service.device.DeviceService;
import li.klass.fhem.service.device.FHTService;
import li.klass.fhem.service.device.GCMSendDeviceService;
import li.klass.fhem.service.device.GenericDeviceService;
import li.klass.fhem.service.device.HeatingService;
import li.klass.fhem.service.device.ToggleableService;
import li.klass.fhem.service.device.WOLService;
import li.klass.fhem.service.graph.GraphService;
import li.klass.fhem.service.room.DeviceListParser;
import li.klass.fhem.service.room.FavoritesService;
import li.klass.fhem.service.room.RoomListHolderService;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.update.UpdateHandler;

@Module(complete = false,
        injects = {
                BillingService.class,
                ConnectionService.class,
                DataConnectionSwitch.class,
                DeviceListParser.class,
                FavoritesService.class,
                RoomListService.class,
                RoomListHolderService.class,
                CommandExecutionService.class,
                FHTService.class,
                HeatingService.class,
                DeviceService.class,
                GenericDeviceService.class,
                GCMSendDeviceService.class,
                WOLService.class,
                AtService.class,
                ToggleableService.class,
                GraphService.class,
                UpdateHandler.class,
                AdvertisementService.class,
                GCMIntentService.class,
                SharedPreferencesService.class,
                DateService.class
        })
public class UtilityServicesModule {
}
