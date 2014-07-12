package li.klass.fhem.appwidget.view.widget.small;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;

import li.klass.fhem.R;
import li.klass.fhem.activities.AndFHEMMainActivity;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.WidgetConfigurationCreatedCallback;
import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.appwidget.view.widget.base.OtherAppWidgetView;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fragments.FragmentType;

import static li.klass.fhem.constants.Actions.WIDGET_REQUEST_UPDATE;

public class DeviceListUpdateWidget extends OtherAppWidgetView {
    @Override
    public void createWidgetConfiguration(Context context, WidgetType widgetType, int appWidgetId, WidgetConfigurationCreatedCallback callback, String... payload) {
        callback.widgetConfigurationCreated(new WidgetConfiguration(appWidgetId, widgetType));
    }

    @Override
    public int getWidgetName() {
        return R.string.widget_device_list_update;
    }

    @Override
    protected int getContentView() {
        return R.layout.appwidget_icon_small;
    }

    @Override
    protected void fillWidgetView(Context context, RemoteViews view, WidgetConfiguration widgetConfiguration) {
        view.setImageViewResource(R.id.icon, R.drawable.launcher_refresh);

        Intent updateIntent = new Intent(WIDGET_REQUEST_UPDATE);
        updateIntent.putExtra("unique", "foobar://" + SystemClock.elapsedRealtime());

        view.setOnClickPendingIntent(R.id.layout, PendingIntent.getService(context,
                widgetConfiguration.widgetId, updateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT));
    }
}