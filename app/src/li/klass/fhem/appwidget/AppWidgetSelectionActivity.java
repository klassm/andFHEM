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

package li.klass.fhem.appwidget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import li.klass.fhem.R;
import li.klass.fhem.activities.core.FragmentBaseActivity;
import li.klass.fhem.appwidget.view.WidgetSize;
import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.appwidget.view.widget.AppWidgetView;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.fragments.device.DeviceNameSelectionFragment;

import java.util.List;

import static android.appwidget.AppWidgetManager.*;

public abstract class AppWidgetSelectionActivity extends FragmentActivity {
    private int widgetId;
    private WidgetSize widgetSize;

    public AppWidgetSelectionActivity(WidgetSize size) {
        this.widgetSize = size;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        widgetId = intent.getExtras().getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);

        if (! intent.getAction().equals(ACTION_APPWIDGET_CONFIGURE) || widgetId == INVALID_APPWIDGET_ID) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        addDeviceSelectionFragment();
    }

    private void addDeviceSelectionFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleExtraKeys.DEVICE_FILTER, new DeviceNameSelectionFragment.DeviceFilter() {
            @Override
            public boolean isSelectable(Device<?> device) {
                return WidgetType.getSupportedWidgetTypesFor(widgetSize, device).size() != 0;
            }
        });

        bundle.putParcelable(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (! resultData.containsKey(BundleExtraKeys.CLICKED_DEVICE)) return;

                Device<?> clickedDevice = (Device<?>) resultData.getSerializable(BundleExtraKeys.CLICKED_DEVICE);
                deviceClicked(clickedDevice);
            }
        });

        DeviceNameSelectionFragment deviceSelectionFragment = new DeviceNameSelectionFragment(bundle);

        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, deviceSelectionFragment)
                    .commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.e(FragmentBaseActivity.class.getName(), "error while switching to fragment " + deviceSelectionFragment.getClass().getName(), e);
        }
    }

    private void deviceClicked(final Device<?> device) {
        final List<WidgetType> widgetTypes = WidgetType.getSupportedWidgetTypesFor(widgetSize, device);
        String[] widgetNames = new String[widgetTypes.size()];

        for (int i = 0; i < widgetTypes.size(); i++) {
            AppWidgetView widgetView = widgetTypes.get(i).widgetView;
            widgetNames[i] = getString(widgetView.getWidgetName());
        }

        final AlertDialog.Builder contextMenu = new AlertDialog.Builder(this);
        contextMenu.setTitle(R.string.widget_type_selection);
        contextMenu.setItems(widgetNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                WidgetType type = widgetTypes.get(position);
                WidgetConfiguration configuration = new WidgetConfiguration(widgetId, device.getName(), type);
                AppWidgetDataHolder.INSTANCE.saveWidgetConfigurationToPreferences(AppWidgetSelectionActivity.this, configuration);

                dialogInterface.dismiss();

                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_APPWIDGET_ID, widgetId);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
        contextMenu.show();
    }
}
