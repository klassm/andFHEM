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
import li.klass.fhem.fragments.core.DeviceDetailFragment;

public abstract class SmallIconWidget extends OtherAppWidgetView {
    @Override
    public void createWidgetConfiguration(Context context, WidgetType widgetType, int appWidgetId, WidgetConfigurationCreatedCallback callback, String... payload) {
        callback.widgetConfigurationCreated(new WidgetConfiguration(appWidgetId, widgetType));
    }

    @Override
    protected int getContentView() {
        return R.layout.appwidget_icon_small;
    }

    @Override
    protected void fillWidgetView(Context context, RemoteViews view, WidgetConfiguration widgetConfiguration) {
        view.setImageViewResource(R.id.icon, getIconResource());

        Intent openIntent = new Intent(context, AndFHEMMainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openIntent.putExtra(BundleExtraKeys.FRAGMENT, getFragment());
        openIntent.putExtra("unique", "foobar://" + SystemClock.elapsedRealtime());

        view.setOnClickPendingIntent(R.id.layout, PendingIntent.getActivity(context,
                widgetConfiguration.widgetId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT));
    }

    protected abstract FragmentType getFragment();

    protected abstract int getIconResource();
}
