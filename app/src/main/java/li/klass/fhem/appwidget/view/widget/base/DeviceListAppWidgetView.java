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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import li.klass.fhem.domain.core.Device;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class DeviceListAppWidgetView extends DeviceAppWidgetView {

    public class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private final Device<?> device;
        private final Context context;
        private final int widgetId;

        public ListRemoteViewsFactory(Context context, Device<?> device, int widgetId) {
            this.context = context;
            this.device = device;
            this.widgetId = widgetId;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            return getListItemCount(device);
        }

        @Override
        public RemoteViews getViewAt(int i) {
            return getRemoteViewAt(context, device, i, widgetId);
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

    public RemoteViewsService.RemoteViewsFactory getRemoteViewsFactory(Context context,
                                                                       Device<?> device,
                                                                       int widgetId) {
        return new ListRemoteViewsFactory(context, device, widgetId);
    }

    protected abstract int getListItemCount(Device<?> device);
    protected abstract RemoteViews getRemoteViewAt(Context context, Device<?> device,
                                                   int position, int widgetId);
}
