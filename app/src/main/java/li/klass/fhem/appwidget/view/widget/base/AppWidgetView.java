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

package li.klass.fhem.appwidget.view.widget.base;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.WidgetConfigurationCreatedCallback;
import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.util.ImageUtil;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AppWidgetView {

    private boolean daggerAttached = false;

    public static final Logger LOG = LoggerFactory.getLogger(AppWidgetView.class);

    public abstract void createWidgetConfiguration(Context context, WidgetType widgetType, int appWidgetId,
                                                   WidgetConfigurationCreatedCallback callback, String... payload);


    public RemoteViews createView(Context context, WidgetConfiguration widgetConfiguration) {
        LOG.debug("creating widget view for configuration {}", widgetConfiguration);

        RemoteViews views = new RemoteViews(context.getPackageName(), getContentView());

        fillWidgetView(context, views, widgetConfiguration);

        return views;
    }

    protected abstract int getContentView();

    protected abstract void fillWidgetView(Context context, RemoteViews view,
                                           WidgetConfiguration widgetConfiguration);

    protected void loadImageAndSetIn(RemoteViews view, int imageId, String url,
                                     boolean preventCache) {
        if (preventCache) {
            url += "?time=" + System.currentTimeMillis();
        }
        Bitmap bitmap = ImageUtil.loadBitmap(url);
        view.setImageViewBitmap(imageId, bitmap);
    }

    protected void setTextViewOrHide(RemoteViews view, int viewId, String value) {
        if (value != null) {
            view.setTextViewText(viewId, value);
            view.setViewVisibility(viewId, View.VISIBLE);
        } else {
            view.setViewVisibility(viewId, View.GONE);
        }
    }

    public abstract int getWidgetName();

    public void attach(Application application) {
        if (!daggerAttached) {
            ((AndFHEMApplication) application).inject(this);
            daggerAttached = true;
        }
    }
}
