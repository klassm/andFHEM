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

@file:Suppress("DEPRECATION")

package li.klass.fhem.graph.ui

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.common.base.Optional
import com.google.common.collect.Lists.newArrayList
import com.google.common.collect.Range
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.activities.core.Updateable
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.devices.ui.ChartMarkerView
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.graph.backend.GraphEntry
import li.klass.fhem.graph.backend.GraphService
import li.klass.fhem.graph.backend.gplot.GPlotSeries
import li.klass.fhem.graph.backend.gplot.SvgGraphDefinition
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.util.DateFormatUtil.ANDFHEM_DATE_TIME_FORMAT
import li.klass.fhem.util.DisplayUtil
import org.jetbrains.anko.coroutines.experimental.bg
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class GraphActivity : AppCompatActivity(), Updateable {

    private var deviceName: String? = null
    private var svgGraphDefinition: SvgGraphDefinition? = null
    private var startDate: DateTime? = null
    private var endDate: DateTime? = null
    private var connectionId: String? = null

    @Inject
    lateinit var deviceListService: DeviceListService

    @Inject
    lateinit var graphService: GraphService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chart)

        (application as AndFHEMApplication).daggerComponent.inject(this)

        if (savedInstanceState != null && savedInstanceState.containsKey(START_DATE)) {
            startDate = savedInstanceState.getSerializable(START_DATE) as DateTime?
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(END_DATE)) {
            endDate = savedInstanceState.getSerializable(END_DATE) as DateTime?
        }

        val extras = intent.extras
        deviceName = extras.getString(DEVICE_NAME)
        connectionId = extras.getString(CONNECTION_ID)

        svgGraphDefinition = extras.getSerializable(DEVICE_GRAPH_DEFINITION) as SvgGraphDefinition


        supportActionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_STANDARD

        update(false)
    }

    override fun update(refresh: Boolean) {
        val name = deviceName ?: return
        async(UI) {
            bg {
                deviceListService.getDeviceForName(name, connectionId)
            }.await()?.let {
                readDataAndCreateChart(it)
            }
        }
    }

    /**
     * Reads all the charting data for a given date and the column specifications set as attribute.
     * @param device    concerned device
     */
    private fun readDataAndCreateChart(device: FhemDevice) {
        val myContext = this
        async(UI) {
            showDialog(DIALOG_EXECUTING)
            val result = bg {
                graphService.getGraphData(device, Optional.absent(), svgGraphDefinition, startDate, endDate, myContext)
            }.await()
            dismissDialog(DIALOG_EXECUTING)

            startDate = result.interval.start
            endDate = result.interval.end
            createChart(device, result.data)
        }
    }

    /**
     * Actually creates the charting view by using the newly read charting data.

     * @param device    concerned device
     * *
     * @param graphData used graph data
     */
    private fun createChart(device: FhemDevice, graphData: Map<GPlotSeries, MutableList<GraphEntry>>) {

        handleDiscreteValues(graphData)
        val lineData = createLineDataFor(graphData)

        val title = if (DisplayUtil.getWidthInDP(applicationContext) < 500) {
            device.aliasOrName + "\n\r" +
                    DATE_TIME_FORMATTER.print(startDate) + " - " + DATE_TIME_FORMATTER.print(endDate)
        } else {
            device.aliasOrName + " " +
                    DATE_TIME_FORMATTER.print(startDate) + " - " + DATE_TIME_FORMATTER.print(endDate)
        }
        supportActionBar!!.title = title

        val lineChart = findViewById<LineChart>(R.id.chart)

        // must be called before setting chart data!
        val plotDefinition = svgGraphDefinition!!.plotDefinition
        setRangeFor(plotDefinition.leftAxis.range, lineChart.axisLeft)
        setRangeFor(plotDefinition.rightAxis.range, lineChart.axisRight)
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IAxisValueFormatter { value, _ -> ANDFHEM_DATE_TIME_FORMAT.print(value.toLong()) }
        xAxis.labelRotationAngle = 300f
        val labelCount = DisplayUtil.getWidthInDP(applicationContext) / 150
        xAxis.setLabelCount(if (labelCount < 2) 2 else labelCount, true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        val description = Description()
        description.text = ""
        lineChart.description = description
        lineChart.setNoDataText(getString(R.string.noGraphEntries))
        lineChart.data = lineData
        lineChart.marker = ChartMarkerView(this)

        lineChart.animateX(200)
    }

    private fun setRangeFor(axisRange: Optional<Range<Double>>, axis: com.github.mikephil.charting.components.YAxis) {
        if (axisRange.isPresent) {
            val range = axisRange.get()
            if (range.hasLowerBound()) {
                axis.axisMinimum = range.lowerEndpoint().toFloat()
            }
            if (range.hasUpperBound()) {
                axis.axisMaximum = range.upperEndpoint().toFloat()
            }
        }
    }

    private fun handleDiscreteValues(graphData: Map<GPlotSeries, MutableList<GraphEntry>>) {
        for ((key, values) in graphData) {
            if (!isDiscreteSeries(key)) {
                continue
            }

            var previousValue = -1f
            val newData = newArrayList<GraphEntry>()

            for (graphEntry in values) {
                val date = graphEntry.date
                val value = graphEntry.value

                if (previousValue == -1f) {
                    previousValue = value
                }

                newData.add(GraphEntry(date.minusMillis(1), previousValue))
                newData.add(GraphEntry(date, value))
                newData.add(GraphEntry(date.plusMillis(1), value))

                previousValue = value
            }
            values.clear()
            values.addAll(newData)
        }
    }

    private fun isDiscreteSeries(plotSeries: GPlotSeries): Boolean {
        val lineType = plotSeries.lineType
        return lineType == GPlotSeries.LineType.STEPS || lineType == GPlotSeries.LineType.FSTEPS || lineType == GPlotSeries.LineType.HISTEPS
    }

    private fun createLineDataFor(graphData: Map<GPlotSeries, MutableList<GraphEntry>>): LineData? {
        val lineDataItems = graphData
                .filter { it.value.isNotEmpty() }
                .map { lineDataSetFrom(it) }.toList()
        return if (lineDataItems.isEmpty()) null else LineData(lineDataItems)
    }

    private fun lineDataSetFrom(entry: Map.Entry<GPlotSeries, MutableList<GraphEntry>>): ILineDataSet {
        val series = entry.key
        val yEntries = entry.value.map { Entry(it.date.millis.toFloat(), it.value) }.toList()

        val lineDataSet = LineDataSet(yEntries, series.title)
        lineDataSet.axisDependency = if (series.axis == GPlotSeries.Axis.LEFT)
            YAxis.AxisDependency.LEFT
        else
            YAxis.AxisDependency.RIGHT
        lineDataSet.color = series.color.hexColor
        lineDataSet.setCircleColor(series.color.hexColor)
        lineDataSet.fillColor = series.color.hexColor
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawValues(false)
        lineDataSet.lineWidth = series.lineWidth

        when (series.seriesType) {
            GPlotSeries.SeriesType.FILL -> lineDataSet.setDrawFilled(true)
            GPlotSeries.SeriesType.DOT -> lineDataSet.enableDashedLine(3f, 2f, 1f)
            else -> {
            }
        }

        when (series.lineType) {
            GPlotSeries.LineType.POINTS -> lineDataSet.enableDashedLine(3f, 2f, 1f)
            else -> {
            }
        }

        if (isDiscreteSeries(series)) {
            lineDataSet.mode = LineDataSet.Mode.STEPPED
        }
        return lineDataSet
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.graph_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            R.id.menu_changeStartEndDate -> {
                startActivityForResult(Intent(this, ChartingDateSelectionActivity::class.java)
                        .putExtra(DEVICE_NAME, deviceName).putExtra(START_DATE, startDate)
                        .putExtra(END_DATE, endDate), REQUEST_TIME_CHANGE)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultIntent)

        if (resultIntent != null && resultCode == Activity.RESULT_OK) {
            val bundle = resultIntent.extras
            when (requestCode) {
                REQUEST_TIME_CHANGE -> {
                    startDate = bundle.getSerializable(START_DATE) as DateTime
                    endDate = bundle.getSerializable(END_DATE) as DateTime
                    update(false)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(START_DATE, startDate)
        outState.putSerializable(END_DATE, endDate)
    }

    @Suppress("OverridingDeprecatedMember")
    override fun onCreateDialog(id: Int): Dialog? {
        super.onCreateDialog(id)

        when (id) {
            DIALOG_EXECUTING -> return ProgressDialog.show(this, "", resources.getString(R.string.executing))
        }
        return null
    }

    companion object {

        private val REQUEST_TIME_CHANGE = 1
        private val DIALOG_EXECUTING = 2

        private val DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd")

        /**
         * Jumps to the charting activity.

         * @param context         calling intent
         * *
         * @param device          concerned device
         * *
         * @param connectionId    connection ID
         * *
         * @param graphDefinition series descriptions each representing one series in the resulting chart
         */
        fun showChart(context: Context, device: FhemDevice, connectionId: String?, graphDefinition: SvgGraphDefinition) {

            context.startActivity(Intent(context, GraphActivity::class.java)
                    .putExtra(DEVICE_NAME, device.name)
                    .putExtra(CONNECTION_ID, connectionId)
                    .putExtra(DEVICE_GRAPH_DEFINITION, graphDefinition))
        }
    }
}
