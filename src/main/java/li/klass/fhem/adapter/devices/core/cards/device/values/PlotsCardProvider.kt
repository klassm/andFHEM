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

package li.klass.fhem.adapter.devices.core.cards.device.values

import android.content.Context
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.google.common.base.Optional
import kotlinx.android.synthetic.main.device_detail_card_plots.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.cards.GenericDetailCardProvider
import li.klass.fhem.domain.GenericDevice
import li.klass.fhem.graph.backend.GraphDefinitionsForDeviceService
import li.klass.fhem.graph.backend.gplot.SvgGraphDefinition
import li.klass.fhem.graph.backend.gplot.SvgGraphDefinition.BY_NAME
import li.klass.fhem.graph.ui.GraphActivity
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.layoutInflater
import org.slf4j.LoggerFactory
import javax.inject.Inject

class PlotsCardProvider @Inject constructor(
        private val graphDefinitionsForDeviceService: GraphDefinitionsForDeviceService
) : GenericDetailCardProvider {
    override fun ordering(): Int = 20

    override fun provideCard(fhemDevice: GenericDevice, context: Context, connectionId: String?): CardView? {
        val cardView = context.layoutInflater.inflate(R.layout.device_detail_card_plots, null) as CardView
        cardView.visibility = View.GONE

        loadGraphs(fhemDevice, cardView, connectionId, context)

        return cardView
    }

    private fun loadGraphs(device: GenericDevice, cardView: CardView, connectionId: String?, context: Context) {
        async(UI) {
            val graphs = bg {
                graphDefinitionsForDeviceService.graphDefinitionsFor(device.xmlListDevice, Optional.fromNullable(connectionId))
            }.await()
            fillPlotsCard(cardView, device, graphs, connectionId, context)

            cardView.invalidate()
        }
    }

    private fun fillPlotsCard(plotsCard: CardView, device: GenericDevice,
                              graphDefinitions: Set<SvgGraphDefinition>,
                              connectionId: String?, context: Context) {
        val definitions = graphDefinitions.sortedWith(BY_NAME)
        if (definitions.isEmpty()) {
            return
        }

        val graphLayout = plotsCard.plotsList
        if (graphLayout == null) {
            logger.error("fillPlotsCard - cannot find graphLayout, is null")
            return
        }

        plotsCard.visibility = View.VISIBLE


        graphLayout.removeAllViews()
        definitions.forEach { svgGraphDefinition ->
            val button = LayoutInflater.from(context).inflate(R.layout.device_detail_card_plots_button, graphLayout, false) as Button
            button.text = svgGraphDefinition.name
            button.setOnClickListener { GraphActivity.showChart(context, device, connectionId, svgGraphDefinition) }
            graphLayout.addView(button)
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(PlotsCardProvider::class.java)
    }
}