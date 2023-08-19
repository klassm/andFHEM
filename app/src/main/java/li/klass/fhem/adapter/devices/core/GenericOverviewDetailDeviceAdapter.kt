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
import android.view.LayoutInflater
import android.view.View
import androidx.navigation.NavController
import li.klass.fhem.adapter.devices.core.cards.GenericDetailCardProviders
import li.klass.fhem.adapter.devices.strategy.StrategyProvider
import li.klass.fhem.adapter.devices.strategy.ViewStrategy
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.databinding.DeviceDetailGenericBinding
import li.klass.fhem.devices.detail.ui.ExpandHandler
import li.klass.fhem.domain.core.FhemDevice
import org.slf4j.LoggerFactory
import javax.inject.Inject

class GenericOverviewDetailDeviceAdapter @Inject constructor(
        private val strategyProvider: StrategyProvider,
        private val genericDetailCardProviders: GenericDetailCardProviders
) : OverviewDeviceAdapter() {
    override fun inject(daggerComponent: ApplicationComponent) {
        daggerComponent.inject(this)
    }

    @SuppressLint("InflateParams")
    suspend fun getDeviceDetailView(context: Context, device: FhemDevice, connectionId: String?, navController: NavController, expandHandler: ExpandHandler): View {
        val binding = DeviceDetailGenericBinding.inflate(LayoutInflater.from(context), null, false)

        val measureTime = xmlDeviceItemProvider.getMostRecentMeasureTime(device, context)
        if (measureTime != null) {
            binding.measureTime.text = measureTime
            binding.measureTimeRow.visibility = View.VISIBLE
        } else {
            binding.measureTimeRow.visibility = View.VISIBLE
        }
        val connection = dataConnectionSwitch.getProviderFor(connectionId)

        genericDetailCardProviders.providers
            .sortedBy { it.ordering() }
            .mapNotNull {
                try {

                    it.provideCard(
                        device,
                        context,
                        connection.server.id,
                        navController,
                        expandHandler
                    )
                } catch (e: Exception) {
                    logger.error(
                        "getDeviceDetailView(device=${device.name}) - error while providing card of ${it.javaClass.name}",
                        e
                    )
                    null
                }
            }
            .forEach { binding.detailLayout.addView(it) }

        return binding.root
    }

    override fun fillOverviewStrategies(overviewStrategies: MutableList<ViewStrategy>) {
        super.fillOverviewStrategies(overviewStrategies)
        strategyProvider.strategies.forEach { overviewStrategies.add(it) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GenericOverviewDetailDeviceAdapter::class.java)
    }
}
