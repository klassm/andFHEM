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

package li.klass.fhem.adapter.devices.core.cards

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import li.klass.fhem.databinding.DeviceDetailCardPlotsBinding
import li.klass.fhem.databinding.DeviceDetailCardPlotsButtonBinding
import li.klass.fhem.devices.detail.ui.ExpandHandler
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.graph.backend.GraphDefinitionsForDeviceService
import li.klass.fhem.graph.backend.gplot.SvgGraphDefinition
import li.klass.fhem.graph.ui.GraphActivity
import org.slf4j.LoggerFactory
import javax.inject.Inject

class PlotsCardProvider @Inject constructor(
        private val graphDefinitionsForDeviceService: GraphDefinitionsForDeviceService
) : GenericDetailCardProvider {
    override fun ordering(): Int = 20

    override suspend fun provideCard(device: FhemDevice, context: Context, connectionId: String, navController: NavController, expandHandler: ExpandHandler): CardView? {
        val binding =
            DeviceDetailCardPlotsBinding.inflate(LayoutInflater.from(context), null, false)
        binding.cardProgress.visibility = View.VISIBLE

        GlobalScope.launch(Dispatchers.Main) {
            loadGraphs(device, binding, connectionId, context)
        }

        return binding.root
    }

    private suspend fun loadGraphs(
        device: FhemDevice,
        binding: DeviceDetailCardPlotsBinding,
        connectionId: String,
        context: Context
    ) {
        val graphs = withContext(Dispatchers.IO) {
            graphDefinitionsForDeviceService.graphDefinitionsFor(device.xmlListDevice, connectionId)
        }
        fillPlotsCard(binding, device, graphs, connectionId, context)
        binding.cardProgress.visibility = View.GONE
        binding.root.invalidate()
    }

    private fun fillPlotsCard(
        binding: DeviceDetailCardPlotsBinding, device: FhemDevice,
        graphDefinitions: Set<SvgGraphDefinition>,
        connectionId: String?, context: Context
    ) {
        val definitions = graphDefinitions.sortedBy { it.name }
        if (definitions.isEmpty()) {
            return
        }

        val graphLayout = binding.plotsList
        graphLayout.removeAllViews()
        definitions.forEach { svgGraphDefinition ->
            val buttonBinding = DeviceDetailCardPlotsButtonBinding.inflate(
                LayoutInflater.from(context),
                graphLayout,
                false
            )
            buttonBinding.button.text = svgGraphDefinition.name
            buttonBinding.button.setOnClickListener {
                GraphActivity.showChart(
                    context,
                    device,
                    connectionId,
                    svgGraphDefinition
                )
            }
            graphLayout.addView(buttonBinding.root)
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(PlotsCardProvider::class.java)!!
    }
}