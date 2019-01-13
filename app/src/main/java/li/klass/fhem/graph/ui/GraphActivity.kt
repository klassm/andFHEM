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
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.common.base.Optional
import com.google.common.collect.Range
import kotlinx.android.synthetic.main.chart.*
import kotlinx.coroutines.*
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
import li.klass.fhem.util.resolveColor
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class GraphActivity : AppCompatActivity(), Updateable {

    private lateinit var deviceName: String
    private lateinit var svgGraphDefinition: SvgGraphDefinition
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

        val extras = intent.extras ?: return
        deviceName = extras.getString(DEVICE_NAME)!!
        connectionId = extras.getString(CONNECTION_ID)

        svgGraphDefinition = extras.getSerializable(DEVICE_GRAPH_DEFINITION) as SvgGraphDefinition


        supportActionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_STANDARD

        GlobalScope.launch(Dispatchers.Main) {
            update(false)
        }
    }

    override suspend fun update(refresh: Boolean) {
        val name = deviceName
        coroutineScope {
            withContext(Dispatchers.IO) {
                deviceListService.getDeviceForName(name, connectionId)
            }?.let {
                readDataAndCreateChart(it)
            }
        }
    }

    /**
     * Reads all the charting data for a given date and the column specifications set as attribute.
     * @param device    concerned device
     */
    private suspend fun readDataAndCreateChart(device: FhemDevice) {
        val myContext = this
        coroutineScope {
            showDialog(DIALOG_EXECUTING)
            val result = withContext(Dispatchers.IO) {
                graphService.getGraphData(device, null, svgGraphDefinition, startDate, endDate, myContext)
            }
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
    private fun createChart(device: FhemDevice, graphData: Map<GPlotSeries, List<GraphEntry>>) {

        val activity = this
        val themeTextColor = theme.resolveColor(android.R.attr.textColorPrimary)

        val processedData = handleDiscreteValues(graphData)
        val lineData = createLineDataFor(processedData)
        val title = if (DisplayUtil.getWidthInDP(applicationContext) < 500) {
            device.aliasOrName + "\n\r" +
                    DATE_TIME_FORMATTER.print(startDate) + " - " + DATE_TIME_FORMATTER.print(endDate)
        } else {
            device.aliasOrName + " " +
                    DATE_TIME_FORMATTER.print(startDate) + " - " + DATE_TIME_FORMATTER.print(endDate)
        }
        supportActionBar!!.title = title

        chart.apply {
            xAxis.apply {
                valueFormatter = IAxisValueFormatter { value, _ -> ANDFHEM_DATE_TIME_FORMAT.print(value.toLong()) }
                labelRotationAngle = 300f
                val labelCount = DisplayUtil.getWidthInDP(applicationContext) / 150
                setLabelCount(if (labelCount < 2) 2 else labelCount, true)
                position = XAxis.XAxisPosition.BOTTOM
                textColor = themeTextColor
            }
            axisLeft.textColor = themeTextColor
            axisRight.textColor = themeTextColor
            legend.textColor = themeTextColor

            val plotDefinition = svgGraphDefinition.plotDefinition
            setRangeFor(plotDefinition.leftAxis.range, axisLeft)
            setRangeFor(plotDefinition.rightAxis.range, axisRight)

            description = Description().apply { text = "" }
            data = lineData
            marker = ChartMarkerView(activity)
            setNoDataText(getString(R.string.noGraphEntries))

            animateX(200)
        }
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

    private fun handleDiscreteValues(graphData: Map<GPlotSeries, List<GraphEntry>>): Map<GPlotSeries, List<GraphEntry>> {
        return graphData.mapValues { (key, values) ->
            if (!isDiscreteSeries(key)) {
                values
            } else handleDiscreteValue(key, values)
        }
    }

    private fun handleDiscreteValue(gPlotSeries: GPlotSeries, values: List<GraphEntry>): List<GraphEntry> {
        if (!isDiscreteSeries(gPlotSeries)) {
            return values
        }

        var previousValue = -1f
        val newData = mutableListOf<GraphEntry>()

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

        return newData
    }

    private fun isDiscreteSeries(plotSeries: GPlotSeries): Boolean {
        val lineType = plotSeries.lineType
        return lineType == GPlotSeries.LineType.STEPS || lineType == GPlotSeries.LineType.FSTEPS || lineType == GPlotSeries.LineType.HISTEPS
    }

    private fun createLineDataFor(graphData: Map<GPlotSeries, List<GraphEntry>>): LineData? {
        val lineDataItems = graphData
                .filter { it.value.isNotEmpty() }
                .map { lineDataSetFrom(it) }.toList()
        return if (lineDataItems.isEmpty()) null else LineData(lineDataItems)
    }

    private fun lineDataSetFrom(entry: Map.Entry<GPlotSeries, List<GraphEntry>>): ILineDataSet {
        val series = entry.key
        val yEntries = entry.value.map { Entry(it.date.millis.toFloat(), it.value) }.toList()

        return LineDataSet(yEntries, series.title).apply {
            axisDependency = if (series.axis == GPlotSeries.Axis.LEFT)
                YAxis.AxisDependency.LEFT
            else
                YAxis.AxisDependency.RIGHT

            val seriesColor = theme.resolveColor(series.color.colorAttribute)
            color = seriesColor
            setCircleColor(seriesColor)
            fillColor = seriesColor
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = series.lineWidth

            when (series.seriesType) {
                GPlotSeries.SeriesType.FILL -> setDrawFilled(true)
                GPlotSeries.SeriesType.DOT -> enableDashedLine(3f, 2f, 1f)
                else -> {
                }
            }

            when (series.lineType) {
                GPlotSeries.LineType.POINTS -> enableDashedLine(3f, 2f, 1f)
                else -> {
                }
            }

            if (isDiscreteSeries(series)) {
                mode = LineDataSet.Mode.STEPPED
            }
        }
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
            val bundle = resultIntent.extras ?: return
            when (requestCode) {
                REQUEST_TIME_CHANGE -> {
                    startDate = bundle.getSerializable(START_DATE) as DateTime
                    endDate = bundle.getSerializable(END_DATE) as DateTime
                    GlobalScope.launch(Dispatchers.Main) {
                        update(false)
                    }
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

        private const val REQUEST_TIME_CHANGE = 1
        private const val DIALOG_EXECUTING = 2

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
