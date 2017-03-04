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

package li.klass.fhem.activities.graph;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Map;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.activities.core.Updateable;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.gplot.GPlotDefinition;
import li.klass.fhem.service.graph.gplot.GPlotSeries;
import li.klass.fhem.service.graph.gplot.SvgGraphDefinition;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.service.intent.RoomListIntentService;
import li.klass.fhem.util.DisplayUtil;
import li.klass.fhem.util.FhemResultReceiver;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.constants.Actions.DEVICE_GRAPH;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_GRAPH_DEFINITION;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_GRAPH_ENTRY_MAP;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;
import static li.klass.fhem.constants.BundleExtraKeys.END_DATE;
import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;
import static li.klass.fhem.constants.BundleExtraKeys.START_DATE;
import static li.klass.fhem.util.DateFormatUtil.ANDFHEM_DATE_FORMAT;
import static org.joda.time.Duration.standardHours;

public class ChartingActivity extends AppCompatActivity implements Updateable {

    private static final int REQUEST_TIME_CHANGE = 1;
    private static final int DIALOG_EXECUTING = 2;

    private static final int CURRENT_DAY_TIMESPAN = -1;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");

    /**
     * Current device graphs are shown for.
     */
    private String deviceName;

    private SvgGraphDefinition svgGraphDefinition;

    /**
     * Start date for the current graph.
     */
    private DateTime startDate = new DateTime();

    /**
     * End date for the current graph
     */
    private DateTime endDate = new DateTime();
    private String connectionId;

    /**
     * Jumps to the charting activity.
     *  @param context         calling intent
     * @param device          concerned device
     * @param connectionId
     * @param graphDefinition series descriptions each representing one series in the resulting chart
     */
    @SuppressWarnings("unchecked")
    public static void showChart(Context context, FhemDevice device, String connectionId, SvgGraphDefinition graphDefinition) {

        context.startActivity(new Intent(context, ChartingActivity.class)
                .putExtra(DEVICE_NAME, device.getName())
                .putExtra(CONNECTION_ID, connectionId)
                .putExtra(DEVICE_GRAPH_DEFINITION, graphDefinition));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart);

        ((AndFHEMApplication) getApplication()).getDaggerComponent().inject(this);

        if (savedInstanceState != null && savedInstanceState.containsKey(START_DATE)) {
            startDate = (DateTime) savedInstanceState.getSerializable(START_DATE);
        } else {
            int defaultTimespan = getChartingDefaultTimespan();
            if (defaultTimespan == CURRENT_DAY_TIMESPAN) {
                startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0, 0);
            } else {
                startDate = startDate.minus(standardHours(defaultTimespan));
            }
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(END_DATE)) {
            endDate = (DateTime) savedInstanceState.getSerializable(END_DATE);
        }

        Bundle extras = getIntent().getExtras();
        deviceName = extras.getString(DEVICE_NAME);
        connectionId = extras.getString(CONNECTION_ID);

        svgGraphDefinition = (SvgGraphDefinition) extras.getSerializable(DEVICE_GRAPH_DEFINITION);


        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        update(false);
    }

    private int getChartingDefaultTimespan() {
        String timeSpan = PreferenceManager.getDefaultSharedPreferences(this).getString("GRAPH_DEFAULT_TIMESPAN", "24");
        return Integer.valueOf(timeSpan.trim());
    }

    @Override
    public void update(final boolean doUpdate) {
        startService(new Intent(Actions.GET_DEVICE_FOR_NAME)
                .setClass(this, RoomListIntentService.class)
                .putExtra(DEVICE_NAME, deviceName)
                .putExtra(CONNECTION_ID, connectionId)
                .putExtra(DO_REFRESH, doUpdate)
                .putExtra(RESULT_RECEIVER, new FhemResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode != ResultCodes.SUCCESS) return;

                        readDataAndCreateChart(doUpdate, (FhemDevice) resultData.getSerializable(DEVICE));
                    }
                }));
    }

    /**
     * Reads all the charting data for a given date and the column specifications set as attribute.
     *
     * @param doRefresh should the underlying room device list be refreshed?
     * @param device    concerned device
     */
    @SuppressWarnings("unchecked")
    private void readDataAndCreateChart(boolean doRefresh, final FhemDevice device) {
        showDialog(DIALOG_EXECUTING);
        startService(new Intent(DEVICE_GRAPH)
                .setClass(this, DeviceIntentService.class)
                .putExtra(DO_REFRESH, doRefresh)
                .putExtra(DEVICE_NAME, deviceName)
                .putExtra(START_DATE, startDate)
                .putExtra(END_DATE, endDate)
                .putExtra(CONNECTION_ID, connectionId)
                .putExtra(DEVICE_GRAPH_DEFINITION, svgGraphDefinition)
                .putExtra(RESULT_RECEIVER, new FhemResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == ResultCodes.SUCCESS) {
                            createChart(device, (Map<GPlotSeries, List<GraphEntry>>) resultData.get(DEVICE_GRAPH_ENTRY_MAP));
                        }

                        try {
                            dismissDialog(DIALOG_EXECUTING);
                        } catch (Exception e) {
                            Log.e(ChartingActivity.class.getName(), "error while hiding dialog", e);
                        }
                    }
                }));
    }

    /**
     * Actually creates the charting view by using the newly read charting data.
     *
     * @param device    concerned device
     * @param graphData used graph data
     */
    @SuppressWarnings("unchecked")
    private void createChart(FhemDevice device, Map<GPlotSeries, List<GraphEntry>> graphData) {

        handleDiscreteValues(graphData);
        LineData lineData = createLineDataFor(graphData);

        String title;
        if (DisplayUtil.getWidthInDP() < 500) {
            title = device.getAliasOrName() + "\n\r" +
                    DATE_TIME_FORMATTER.print(startDate) + " - " + DATE_TIME_FORMATTER.print(endDate);
        } else {
            title = device.getAliasOrName() + " " +
                    DATE_TIME_FORMATTER.print(startDate) + " - " + DATE_TIME_FORMATTER.print(endDate);
        }
        getSupportActionBar().setTitle(title);

        LineChart lineChart = (LineChart) findViewById(R.id.chart);

        // must be called before setting chart data!
        GPlotDefinition plotDefinition = svgGraphDefinition.getPlotDefinition();
        setRangeFor(plotDefinition.getLeftAxis().getRange(), lineChart.getAxisLeft());
        setRangeFor(plotDefinition.getRightAxis().getRange(), lineChart.getAxisRight());
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return ANDFHEM_DATE_FORMAT.print((long) value);
            }
        });
        xAxis.setLabelRotationAngle(300);
        int labelCount = DisplayUtil.getWidthInDP() / 150;
        xAxis.setLabelCount(labelCount < 2 ? 2 : labelCount, true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);
        lineChart.setNoDataText(getString(R.string.noGraphEntries));
        lineChart.setData(lineData);
        lineChart.setMarkerView(new ChartMarkerView(this));

        lineChart.animateX(200);
    }

    private void setRangeFor(Optional<Range<Double>> axisRange, com.github.mikephil.charting.components.YAxis axis) {
        if (axisRange.isPresent()) {
            Range<Double> range = axisRange.get();
            if (range.hasLowerBound()) {
                axis.setAxisMinimum(range.lowerEndpoint().floatValue());
            }
            if (range.hasUpperBound()) {
                axis.setAxisMaximum(range.upperEndpoint().floatValue());
            }
        }
    }

    private void handleDiscreteValues(Map<GPlotSeries, List<GraphEntry>> graphData) {
        for (Map.Entry<GPlotSeries, List<GraphEntry>> entry : graphData.entrySet()) {
            if (!isDiscreteSeries(entry.getKey())) {
                continue;
            }

            float previousValue = -1;
            List<GraphEntry> newData = newArrayList();

            List<GraphEntry> values = entry.getValue();
            for (GraphEntry graphEntry : values) {
                DateTime date = graphEntry.getDate();
                float value = graphEntry.getValue();

                if (previousValue == -1) {
                    previousValue = value;
                }

                newData.add(new GraphEntry(date.minusMillis(1), previousValue));
                newData.add(new GraphEntry(date, value));
                newData.add(new GraphEntry(date.plusMillis(1), value));

                previousValue = value;
            }
            values.clear();
            values.addAll(newData);
        }
    }

    private boolean isDiscreteSeries(GPlotSeries plotSeries) {
        GPlotSeries.LineType lineType = plotSeries.getLineType();
        return lineType == GPlotSeries.LineType.STEPS || lineType == GPlotSeries.LineType.FSTEPS || lineType == GPlotSeries.LineType.HISTEPS;
    }

    private LineData createLineDataFor(Map<GPlotSeries, List<GraphEntry>> graphData) {
        ImmutableList<ILineDataSet> lineDataItems = from(graphData.entrySet())
                .transform(new Function<Map.Entry<GPlotSeries, List<GraphEntry>>, ILineDataSet>() {
                    @Override
                    public ILineDataSet apply(Map.Entry<GPlotSeries, List<GraphEntry>> input) {
                        return lineDataSetFrom(input);
                    }
                })
                .toList();

        return new LineData(lineDataItems);
    }

    private ILineDataSet lineDataSetFrom(Map.Entry<GPlotSeries, List<GraphEntry>> entry) {
        GPlotSeries series = entry.getKey();
        ImmutableList<Entry> yEntries = from(entry.getValue()).transform(new Function<GraphEntry, Entry>() {
            @Override
            public Entry apply(GraphEntry input) {
                assert input != null;
                return new Entry(input.getDate().getMillis(), input.getValue());
            }
        }).toList();


        LineDataSet lineDataSet = new LineDataSet(yEntries, series.getTitle());
        lineDataSet.setAxisDependency(series.getAxis() == GPlotSeries.Axis.LEFT ?
                YAxis.AxisDependency.LEFT :
                YAxis.AxisDependency.RIGHT);
        lineDataSet.setColor(series.getColor().getHexColor());
        lineDataSet.setCircleColor(series.getColor().getHexColor());
        lineDataSet.setFillColor(series.getColor().getHexColor());
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setLineWidth(series.getLineWidth());

        switch (series.getSeriesType()) {
            case FILL:
                lineDataSet.setDrawFilled(true);

                break;
            case DOT:
                lineDataSet.enableDashedLine(3, 2, 1);
                break;
        }

        switch (series.getLineType()) {
            case POINTS:
                lineDataSet.enableDashedLine(3, 2, 1);
                break;
        }

        if (isDiscreteSeries(series)) {
            lineDataSet.setMode(LineDataSet.Mode.STEPPED);
        }
        return lineDataSet;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.graph_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_changeStartEndDate:
                startActivityForResult(new Intent(this, ChartingDateSelectionActivity.class)
                        .putExtra(DEVICE_NAME, deviceName).putExtra(START_DATE, startDate)
                        .putExtra(END_DATE, endDate), REQUEST_TIME_CHANGE);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);

        if (resultIntent != null && resultCode == RESULT_OK) {
            Bundle bundle = resultIntent.getExtras();
            switch (requestCode) {
                case REQUEST_TIME_CHANGE:
                    startDate = (DateTime) bundle.getSerializable(START_DATE);
                    endDate = (DateTime) bundle.getSerializable(END_DATE);
                    update(false);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(START_DATE, startDate);
        outState.putSerializable(END_DATE, endDate);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        super.onCreateDialog(id);

        switch (id) {
            case DIALOG_EXECUTING:
                return ProgressDialog.show(this, "", getResources().getString(R.string.executing));
        }
        return null;
    }
}
