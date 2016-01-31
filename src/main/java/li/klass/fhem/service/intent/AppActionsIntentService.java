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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.ResultReceiver;
import android.provider.AlarmClock;
import android.util.Log;

import com.google.common.base.Optional;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import javax.inject.Inject;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.exception.CommandExecutionException;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.device.DeviceService;
import li.klass.fhem.service.device.GenericDeviceService;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.util.ApplicationProperties;

public class AppActionsIntentService extends ConvenientIntentService {

    public static final int ALARM_CLOCK_UPDATE_INTERVAL = 6 * 60 * 60 * 1000;

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    RoomListService roomListService;

    @Inject
    GenericDeviceService deviceService;

    public static final Logger LOGGER = LoggerFactory.getLogger(AppActionsIntentService.class);

    public AppActionsIntentService() {
        super(AppActionsIntentService.class.getName());
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        String action = intent.getAction();
        if (Actions.LOAD_PROPERTIES.equals(action)) {
            applicationProperties.load();
            return STATE.SUCCESS;
        } else if (Actions.UPDATE_NEXT_ALARM_CLOCK.equals(action) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Optional<FhemDevice> nextAlarmClockReceiver = roomListService.getDeviceForName("nextAlarmClock", this);
            if (nextAlarmClockReceiver.isPresent()) {
                AlarmManager.AlarmClockInfo nextAlarmClock = getAlarmManager().getNextAlarmClock();
                if (nextAlarmClock != null) {
                    long triggerTime = nextAlarmClock.getTriggerTime();
                    String time = new DateTime(new Date(triggerTime)).toString("dd.mm.YYYY HH:MM");
                    LOGGER.info("handleIntent() - notifying allarm clock receiver for time {}", time);
                    deviceService.setState(nextAlarmClockReceiver.get(), time, this);
                }
            } else {
                LOGGER.info("handleIntent() - found no alarm clock receiver");
            }
        } else if (Actions.SCHEDULE_ALARM_CLOCK_UPDATE.equals(action) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LOGGER.info("handleIntent() - schedule alarm clock update");
            AlarmManager alarmManager = getAlarmManager();
            PendingIntent pendingIntent = getNextAlarmClockPendingIntent();
            alarmManager.cancel(pendingIntent);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, ALARM_CLOCK_UPDATE_INTERVAL, pendingIntent);
        }

        return STATE.DONE;
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }

    private PendingIntent getNextAlarmClockPendingIntent() {
        Intent intent = new Intent(Actions.UPDATE_NEXT_ALARM_CLOCK);

        return PendingIntent.getService(this, "nextAlarmClock".hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
