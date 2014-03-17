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

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.activities.base.DeviceNameSelectionActivity;
import li.klass.fhem.appwidget.view.WidgetSize;
import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.appwidget.view.widget.base.AppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_CONFIGURE;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public abstract class AppWidgetSelectionActivity extends DeviceNameSelectionActivity {

    private int widgetId;
    private WidgetSize widgetSize;

    public AppWidgetSelectionActivity(WidgetSize size) {
        super();
        this.widgetSize = size;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);

        if (! ACTION_APPWIDGET_CONFIGURE.equals(intent.getAction())
                || widgetId == INVALID_APPWIDGET_ID) {

            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected boolean isSelectable(Device<?> device) {
        return WidgetType.getSupportedWidgetTypesFor(widgetSize, device).size() != 0;
    }

    @Override
    protected void deviceClicked(final Device<?> device) {
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
                dialogInterface.dismiss();

                WidgetType type = widgetTypes.get(position);
                type.createWidgetConfiguration
                        (AppWidgetSelectionActivity.this, widgetId, device, new WidgetConfigurationCreatedCallback() {
                            @Override
                            public void widgetConfigurationCreated(WidgetConfiguration widgetConfiguration) {

                                AppWidgetDataHolder.INSTANCE.saveWidgetConfigurationToPreferences(widgetConfiguration);

                                Intent intent = new Intent(Actions.REDRAW_WIDGET);
                                intent.putExtra(BundleExtraKeys.APP_WIDGET_ID, widgetId);
                                startService(intent);

                                Intent resultIntent = new Intent();
                                resultIntent.putExtra(EXTRA_APPWIDGET_ID, widgetId);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            }
                        });
            }
        });
        contextMenu.show();
    }
}
