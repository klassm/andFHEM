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
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.adapter.WidgetDeviceSelectionAdapter;
import li.klass.fhem.appwidget.view.WidgetSize;
import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.appwidget.view.widget.AppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.util.DialogUtil;
import li.klass.fhem.widget.NestedListView;

import java.util.List;

import static android.appwidget.AppWidgetManager.*;

public abstract class AppWidgetSelectionActivity extends ListActivity {
    private int widgetId;
    private WidgetSize widgetSize;

    public AppWidgetSelectionActivity(WidgetSize size) {
        this.widgetSize = size;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NestedListView nestedListView = new NestedListView(this);
        nestedListView.setId(android.R.id.list);
        setContentView(nestedListView);

        Intent intent = getIntent();
        widgetId = intent.getExtras().getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);


        if (! intent.getAction().equals(ACTION_APPWIDGET_CONFIGURE) || widgetId == INVALID_APPWIDGET_ID) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        Intent allDevicesIntent = new Intent(Actions.GET_ALL_ROOMS_DEVICE_LIST);
        allDevicesIntent.putExtras(new Bundle());
        allDevicesIntent.putExtra(BundleExtraKeys.DO_REFRESH, false);
        allDevicesIntent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);

                if (resultCode != ResultCodes.SUCCESS || ! resultData.containsKey(BundleExtraKeys.DEVICE_LIST)) {
                    return;
                }

                RoomDeviceList roomDeviceList = (RoomDeviceList) resultData.getSerializable(BundleExtraKeys.DEVICE_LIST);
                removeDevicesWithoutWidgets(roomDeviceList);

                if (roomDeviceList.getAllDevices().size() == 0) {
                    showNoWidgetAvailableDialog();
                }

                WidgetDeviceSelectionAdapter adapter = new WidgetDeviceSelectionAdapter(AppWidgetSelectionActivity.this, roomDeviceList);
                adapter.addParentChildObserver(new NestedListView.NestedListViewOnClickObserver() {
                    @Override
                    public void onItemClick(View view, Object parent, Object child, int parentPosition, int childPosition) {
                        if (childPosition == -1) return;
                        deviceClickedInMainList((Device<?>) child);
                    }
                });
                setListAdapter(adapter);
            }
        });
        startService(allDevicesIntent);
    }

    private void showNoWidgetAvailableDialog() {
        DialogUtil.showAlertDialog(this, -1, R.string.widget_devicelist_empty,
                new DialogUtil.AlertOnClickListener() {
                    @Override
                    public void onClick() {
                        finish();
                    }
                });
    }

    private void removeDevicesWithoutWidgets(RoomDeviceList roomDeviceList) {
        for (Device device : roomDeviceList.getAllDevices()) {
            if (WidgetType.getSupportedWidgetTypesFor(widgetSize, device).size() == 0) {
                roomDeviceList.removeDevice(device);
            }
        }
    }

    private void deviceClickedInMainList(final Device<?> device) {
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
