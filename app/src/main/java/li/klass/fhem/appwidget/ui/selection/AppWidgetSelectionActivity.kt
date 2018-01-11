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

package li.klass.fhem.appwidget.ui.selection

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager.*
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.appwidget.ui.widget.WidgetConfigurationCreatedCallback
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetTypeProvider
import li.klass.fhem.appwidget.ui.widget.base.AppWidgetView
import li.klass.fhem.appwidget.update.AppWidgetInstanceManager
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.DialogUtil
import org.jetbrains.anko.coroutines.experimental.bg
import javax.inject.Inject

abstract class AppWidgetSelectionActivity(private val widgetSize: WidgetSize) : AppCompatActivity(), SelectionCompletedCallback {
    @Inject
    lateinit var appWidgetInstanceManager: AppWidgetInstanceManager
    @Inject
    lateinit var applicationProperties: ApplicationProperties
    @Inject
    lateinit var appWidgetUpdateService: AppWidgetUpdateService
    @Inject
    lateinit var widgetTypeProvider: WidgetTypeProvider

    private var widgetId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inject((application as AndFHEMApplication).daggerComponent)

        widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)

        if (ACTION_APPWIDGET_CONFIGURE != intent.action || widgetId == INVALID_APPWIDGET_ID) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        if (applicationProperties.getStringSharedPreference(SettingsKeys.STARTUP_PASSWORD, null) != null) {
            DialogUtil.showAlertDialog(this, R.string.app_title, R.string.widget_application_password) {
                finish()
                setResult(Activity.RESULT_CANCELED)
            }
        } else {
            setContentView(R.layout.appwidget_selection)
            val viewPager = findViewById<ViewPager>(R.id.viewpager)!!
            viewPager.adapter = AppWidgetSelectionFragmentAdapter(supportFragmentManager, widgetTypeProvider, this, widgetSize, this)

            val tabLayout = findViewById<TabLayout>(R.id.sliding_tabs)!!
            tabLayout.setupWithViewPager(viewPager)
        }
    }

    private fun openWidgetSelection(widgets: List<AppWidgetView>, vararg payload: String) {
        val widgetNames = widgets.map { getString(it.getWidgetName()) }.toTypedArray()
        AlertDialog.Builder(this)
                .setTitle(R.string.widget_type_selection)
                .setItems(widgetNames) { dialogInterface, position ->
                    dialogInterface.dismiss()

                    val widget = widgets[position]
                    createWidget(widget, *payload)
                }.show()
    }

    private fun createWidget(widget: AppWidgetView, vararg payload: String) {
        widget.createWidgetConfiguration(this, widgetId, object : WidgetConfigurationCreatedCallback {
            override fun widgetConfigurationCreated(widgetConfiguration: WidgetConfiguration) {
                appWidgetInstanceManager.save(widgetConfiguration)

                async(UI) {
                    bg {
                        appWidgetUpdateService.updateWidget(widgetId)
                    }
                }

                setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_APPWIDGET_ID, widgetId))
                finish()
            }
        }, *payload)
    }

    override fun onRoomSelect(roomName: String) {
        val widgets = widgetTypeProvider.getSupportedRoomWidgetsFor(widgetSize)
        openWidgetSelection(widgets, roomName)
    }

    override fun onDeviceSelect(clickedDevice: FhemDevice) {
        val widgetTypes = widgetTypeProvider.getSupportedDeviceWidgetsFor(widgetSize, clickedDevice)
        openWidgetSelection(widgetTypes, clickedDevice.name)
    }

    override fun onOtherWidgetSelect(widgetType: AppWidgetView) {
        createWidget(widgetType)
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    protected abstract fun inject(applicationComponent: ApplicationComponent)
}
