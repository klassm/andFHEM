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

package li.klass.fhem.adapter.devices.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.cards.GenericDetailCardProviders
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider
import li.klass.fhem.adapter.devices.strategy.StrategyProvider
import li.klass.fhem.adapter.devices.strategy.ViewStrategy
import li.klass.fhem.adapter.devices.toggle.OnOffBehavior
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.domain.GenericDevice
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.graph.backend.gplot.SvgGraphDefinition
import org.jetbrains.anko.layoutInflater
import javax.inject.Inject

class GenericOverviewDetailDeviceAdapter : OverviewDeviceAdapter() {

    @Inject
    lateinit var strategyProvider: StrategyProvider
    lateinit var onOffBehavior: OnOffBehavior
    @Inject
    lateinit var deviceHookProvider: DeviceHookProvider
    @Inject
    lateinit var genericDetailCardProviders: GenericDetailCardProviders

    override fun inject(daggerComponent: ApplicationComponent) {
        daggerComponent.inject(this)
    }

    override fun getSupportedDeviceClass(): Class<out FhemDevice> = GenericDevice::class.java

    override fun supportsDetailView(device: FhemDevice): Boolean = true

    @SuppressLint("InflateParams")
    override fun getDeviceDetailView(context: Context, device: FhemDevice, graphDefinitions: Set<SvgGraphDefinition>, connectionId: String?): View? {
        val linearLayout = context.layoutInflater.inflate(R.layout.device_detail_generic, null) as LinearLayout

        genericDetailCardProviders.providers.sortedBy { it.ordering() }
                .map { it.provideCard(device as GenericDevice, context, connectionId) }
                .filter { it != null }
                .forEach { linearLayout.addView(it) }

        return linearLayout
    }

    override fun loadGraphs(): Boolean = false

    override fun attachGraphs(context: Context, detailView: View, graphDefinitions: Set<SvgGraphDefinition>, connectionId: String, device: FhemDevice) {}

    override fun onFillDeviceDetailIntent(context: Context, device: FhemDevice, intent: Intent): Intent =
            intent

    override fun fillOverviewStrategies(overviewStrategies: MutableList<ViewStrategy>) {
        super.fillOverviewStrategies(overviewStrategies)
        strategyProvider.strategies.forEach { overviewStrategies.add(it) }
    }
}
