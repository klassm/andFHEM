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

package li.klass.fhem.appwidget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.service.AppWidgetUpdateService;
import li.klass.fhem.appwidget.view.WidgetSize;
import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.appwidget.view.widget.base.AppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.settings.SettingsKeys;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.DialogUtil;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_CONFIGURE;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public abstract class AppWidgetSelectionActivity extends AppCompatActivity implements SelectionCompletedCallback, Serializable {
    @Inject
    AppWidgetDataHolder appWidgetDataHolder;

    @Inject
    ApplicationProperties applicationProperties;

    private int widgetId;
    private WidgetSize widgetSize;

    public AppWidgetSelectionActivity(WidgetSize size) {
        super();
        this.widgetSize = size;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inject(((AndFHEMApplication) getApplication()).getDaggerComponent());

        Intent intent = getIntent();
        widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);

        if (!ACTION_APPWIDGET_CONFIGURE.equals(intent.getAction())
                || widgetId == INVALID_APPWIDGET_ID) {

            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        if (applicationProperties.getStringSharedPreference(SettingsKeys.STARTUP_PASSWORD, null, this) != null) {
            DialogUtil.showAlertDialog(this, R.string.app_title, R.string.widget_application_password, new DialogUtil.AlertOnClickListener() {
                @Override
                public void onClick() {
                    finish();
                    setResult(RESULT_CANCELED);
                }
            });
        } else {
            setContentView(R.layout.appwidget_selection);
            ViewPager viewPager = findViewById(R.id.viewpager);
            assert viewPager != null;
            viewPager.setAdapter(new AppWidgetSelectionFragmentAdapter(getSupportFragmentManager(), this, widgetSize, this));

            TabLayout tabLayout = findViewById(R.id.sliding_tabs);
            assert tabLayout != null;
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    protected abstract void inject(ApplicationComponent applicationComponent);


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
                appWidgetDataHolder.saveWidgetConfigurationToPreferences(widgetConfiguration, AppWidgetSelectionActivity.this);

                Intent intent = new Intent(Actions.REDRAW_WIDGET);
                intent.setClass(AppWidgetSelectionActivity.this, AppWidgetUpdateService.class);
                intent.putExtra(BundleExtraKeys.APP_WIDGET_ID, widgetId);
                startService(intent);

                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_APPWIDGET_ID, widgetId);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        }, payload);
    }


    @Override
    public void onRoomSelect(String roomName) {
        List<WidgetType> widgetTypes = WidgetType.getSupportedRoomWidgetsFor(widgetSize);
        openWidgetTypeSelection(widgetTypes, roomName);
    }

    @Override
    public void onDeviceSelect(FhemDevice clickedDevice) {

        List<WidgetType> widgetTypes = WidgetType.getSupportedDeviceWidgetsFor(widgetSize, clickedDevice, getApplicationContext());
        openWidgetTypeSelection(widgetTypes, clickedDevice.getName());
    }

    @Override
    public void onOtherWidgetSelect(WidgetType widgetType) {
        createWidget(widgetType);
    }
}
