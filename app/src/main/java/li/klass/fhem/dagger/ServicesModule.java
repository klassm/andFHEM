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
import li.klass.fhem.appwidget.service.AppWidgetUpdateService;
import li.klass.fhem.service.intent.AppActionsIntentService;
import li.klass.fhem.service.intent.ConnectionsIntentService;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.service.intent.ExternalApiService;
import li.klass.fhem.service.intent.FavoritesIntentService;
import li.klass.fhem.service.intent.ImageIntentService;
import li.klass.fhem.service.intent.LicenseIntentService;
import li.klass.fhem.service.intent.NotificationIntentService;
import li.klass.fhem.service.intent.RoomListIntentService;
import li.klass.fhem.service.intent.RoomListUpdateIntentService;
import li.klass.fhem.service.intent.SendCommandIntentService;

@Module(complete = false,
        injects = {
                ConnectionsIntentService.class,
                SendCommandIntentService.class,
                RoomListIntentService.class,
                RoomListUpdateIntentService.class,
                NotificationIntentService.class,
                ImageIntentService.class,
                FavoritesIntentService.class,
                ExternalApiService.class,
                DeviceIntentService.class,
                AppWidgetUpdateService.class,
                LicenseIntentService.class,
                AppActionsIntentService.class
        })
public class ServicesModule {
}
