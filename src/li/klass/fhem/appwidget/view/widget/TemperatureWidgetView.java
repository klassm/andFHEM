/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
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
 */

package li.klass.fhem.appwidget.view.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;
import li.klass.fhem.R;
import li.klass.fhem.activities.MainActivity;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureAdditionalField;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.Device;
import li.klass.fhem.fragments.core.DeviceDetailFragment;

import java.lang.reflect.Field;
import java.util.List;

import static li.klass.fhem.util.ReflectionUtil.*;

public class TemperatureWidgetView extends AppWidgetView {
    @Override
    public int getWidgetName() {
        return R.string.widget_temperature;
    }

    @Override
    public int getContentView() {
        return R.layout.appwidget_temperature;
    }

    @Override
    public void fillWidgetView(Context context, RemoteViews view, Device<?> device, WidgetConfiguration widgetConfiguration) {
        Class<? extends Device> clazz = device.getClass();

        String temperature = getStringForAnnotation(device, WidgetTemperatureField.class);
        List<Field> additionalFields = getFieldsWithAnnotation(clazz, WidgetTemperatureAdditionalField.class);

        switch (additionalFields.size()) {
            case 0:
                view.setViewVisibility(R.id.additional, View.GONE);
                break;
            case 1:
                Field field = additionalFields.get(0);
                WidgetTemperatureAdditionalField annotation = field.getAnnotation(WidgetTemperatureAdditionalField.class);
                String text = getFieldValue(field, device);
                if (annotation.descriptionId() != -1) text += " " + context.getString(annotation.descriptionId());

                view.setTextViewText(R.id.additional, text);
                break;
            default:
                throw new IllegalArgumentException("invalid input for temperature widget for class " + clazz.getName());
        }

        view.setTextViewText(R.id.temperature, temperature);

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openIntent.putExtra(BundleExtraKeys.FRAGMENT_NAME, DeviceDetailFragment.class.getName());
        openIntent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        openIntent.putExtra("unique", "foobar://" + SystemClock.elapsedRealtime());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, widgetConfiguration.widgetId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.main, pendingIntent);

    }

    @Override
    public long updateInterval() {
        return 3600000; // every hour
    }
}
