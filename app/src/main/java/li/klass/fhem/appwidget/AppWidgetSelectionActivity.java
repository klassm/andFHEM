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
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.activities.core.FragmentBaseActivity;
import li.klass.fhem.appwidget.view.WidgetSize;
import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.appwidget.view.widget.base.AppWidgetView;
import li.klass.fhem.appwidget.view.widget.base.otherWidgets.OtherWidgetsFragment;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.fragments.RoomListFragment;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.fragments.device.DeviceNameSelectionFragment;
import li.klass.fhem.util.DialogUtil;
import li.klass.fhem.util.FhemResultReceiver;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_CONFIGURE;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;
import static li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_SIZE;

public abstract class AppWidgetSelectionActivity extends SherlockFragmentActivity implements ActionBar.TabListener {

    public static final int TAG_DEVICES = 0;
    public static final int TAG_ROOMS = 1;
    public static final int TAG_OTHER = 2;

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

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(actionBar.newTab().setText(R.string.widget_devices)
                .setTabListener(this).setTag(TAG_DEVICES));
        actionBar.addTab(actionBar.newTab().setText(R.string.widget_rooms)
                .setTabListener(this).setTag(TAG_ROOMS));
        actionBar.addTab(actionBar.newTab().setText(R.string.widget_others)
                .setTabListener(this).setTag(TAG_OTHER));

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        switchToDevices();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Integer tag = (Integer) tab.getTag();
        switch (tag) {
            case TAG_DEVICES:
                switchToDevices();
                break;
            case TAG_ROOMS:
                switchToRooms();
                break;
            case TAG_OTHER:
                switchToOthers();
                break;
            default:
                throw new IllegalStateException("don't know about " + tag);
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        onTabSelected(tab, fragmentTransaction);
    }

    private void switchToDevices() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleExtraKeys.DEVICE_FILTER, new DeviceNameSelectionFragment.DeviceFilter() {
            @Override
            public boolean isSelectable(Device<?> device) {
                return ! WidgetType.getSupportedDeviceWidgetsFor(widgetSize, device).isEmpty();
            }
        });

        bundle.putParcelable(BundleExtraKeys.RESULT_RECEIVER, new FhemResultReceiver() {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != ResultCodes.SUCCESS ||
                        !resultData.containsKey(BundleExtraKeys.CLICKED_DEVICE)) return;

                Device<?> clickedDevice = (Device<?>) resultData.getSerializable(BundleExtraKeys.CLICKED_DEVICE);
                deviceClicked(clickedDevice);
            }
        });

        DeviceNameSelectionFragment deviceSelectionFragment = new DeviceNameSelectionFragment() {
            @Override
            protected int getEmptyTextId() {
                return R.string.widgetNoDevices;
            }
        };
        deviceSelectionFragment.setArguments(bundle);

        switchTo(deviceSelectionFragment);
    }

    private void switchToRooms() {
        switchTo(new RoomListFragment() {
            @Override
            protected void onClick(String roomName) {
                roomClicked(roomName);
            }

            @Override
            protected boolean isRoomSelectable(String roomName) {
                return ! WidgetType.getSupportedRoomWidgetsFor(widgetSize).isEmpty();
            }

            @Override
            protected int getEmptyTextId() {
                return R.string.widgetNoRooms;
            }
        });
    }

    private void switchToOthers() {
        OtherWidgetsFragment otherWidgetsFragment = new OtherWidgetsFragment() {
            @Override
            protected void onClick(WidgetType type) {
                createWidget(type);
            }
        };

        Bundle arguments = new Bundle();
        arguments.putSerializable(APP_WIDGET_SIZE, widgetSize);
        otherWidgetsFragment.setArguments(arguments);

        switchTo(otherWidgetsFragment);
    }

    private void switchTo(BaseFragment fragment) {
        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.e(FragmentBaseActivity.class.getName(), "error while switching to fragment " +
                    DeviceNameSelectionFragment.class.getName(), e);
        }
    }

    private void deviceClicked(Device<?> device) {
        final List<WidgetType> widgetTypes = WidgetType.getSupportedDeviceWidgetsFor(widgetSize, device);
        openWidgetTypeSelection(widgetTypes, device.getName());
    }

    private void roomClicked(String roomName) {
        final List<WidgetType> widgetTypes = WidgetType.getSupportedRoomWidgetsFor(widgetSize);
        openWidgetTypeSelection(widgetTypes, roomName);
    }

    private void openWidgetTypeSelection(final List<WidgetType> widgetTypes, final String... payload) {
        String[] widgetNames = new String[widgetTypes.size()];
        for (int i = 0; i < widgetTypes.size(); i++) {
            AppWidgetView widgetView = widgetTypes.get(i).widgetView;
            widgetNames[i] = getString(widgetView.getWidgetName());
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.widget_type_selection)
                .setItems(widgetNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        dialogInterface.dismiss();

                        WidgetType type = widgetTypes.get(position);
                        createWidget(type, payload);
                    }
                }).show();
    }

    private void createWidget(WidgetType type, String... payload) {
        type.createWidgetConfiguration(this, widgetId, new WidgetConfigurationCreatedCallback() {
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
                }, payload);
    }
}
