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

package li.klass.fhem.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import li.klass.fhem.R;

public class NotificationUtil {

    private static final String NOTIFICATION_CHANNEL = "ANDFHEM_NOTIFICATION";

    public static void notify(Context context, int notifyId, PendingIntent pendingIntent,
                              String contentTitle, String contentText, String tickerText,
                              boolean vibrate) {

        createChannel(context);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setTicker(tickerText)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLights(0xff00960b, 1000, 5000)
                .setSound(alarmSound)
                .setLargeIcon(
                        BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher)
                )
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (vibrate) {
            notificationBuilder.setVibrate(new long[]{100, 400});
        }

        Notification notification = notificationBuilder.build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifyId, notification);
    }

    private static void createChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String name = "andFHEM Notification";
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(name);
            channel.setShowBadge(true);
            channel.setLightColor(0xff00960b);
            channel.setBypassDnd(true);
            channel.enableLights(true);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
