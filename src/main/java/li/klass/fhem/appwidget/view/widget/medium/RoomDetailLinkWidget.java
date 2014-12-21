package li.klass.fhem.appwidget.view.widget.medium;

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
import li.klass.fhem.appwidget.view.widget.base.RoomAppWidgetView;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fragments.FragmentType;

public class RoomDetailLinkWidget extends RoomAppWidgetView {
    @Override
    public void createWidgetConfiguration(Context context, WidgetType widgetType, int appWidgetId, WidgetConfigurationCreatedCallback callback, String... payload) {
        callback.widgetConfigurationCreated(new WidgetConfiguration(appWidgetId, widgetType, payload));
    }

    @Override
    public int getWidgetName() {
        return R.string.widget_room_detail;
    }

    @Override
    protected int getContentView() {
        return R.layout.appwidget_room_link;
    }

    @Override
    protected void fillWidgetView(Context context, RemoteViews view, WidgetConfiguration widgetConfiguration) {
        String roomName = widgetConfiguration.payload.get(0);

        view.setTextViewText(R.id.roomName, roomName);

        Intent openIntent = new Intent(context, AndFHEMMainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openIntent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.ROOM_DETAIL);
        openIntent.putExtra(BundleExtraKeys.ROOM_NAME, roomName);
        openIntent.putExtra("unique", "foobar://" + SystemClock.elapsedRealtime());

        view.setOnClickPendingIntent(R.id.layout, PendingIntent.getActivity(context,
                widgetConfiguration.widgetId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
