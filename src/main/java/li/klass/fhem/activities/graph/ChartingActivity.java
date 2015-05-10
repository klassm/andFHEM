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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.common.collect.ImmutableList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.BasicStroke;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine;
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
import li.klass.fhem.service.graph.gplot.GPlotAxis;
import li.klass.fhem.service.graph.gplot.GPlotDefinition;
import li.klass.fhem.service.graph.gplot.GPlotSeries;
import li.klass.fhem.service.graph.gplot.SvgGraphDefinition;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.service.intent.RoomListIntentService;
import li.klass.fhem.util.DisplayUtil;
import li.klass.fhem.util.FhemResultReceiver;

import static com.google.common.collect.Sets.newHashSet;
import static li.klass.fhem.constants.Actions.DEVICE_GRAPH;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_GRAPH_DEFINITION;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_GRAPH_ENTRY_MAP;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;
import static li.klass.fhem.constants.BundleExtraKeys.END_DATE;
import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;
import static li.klass.fhem.constants.BundleExtraKeys.START_DATE;
import static li.klass.fhem.util.DisplayUtil.dpToPx;
import static org.joda.time.Duration.standardHours;

public class ChartingActivity extends ActionBarActivity implements Updateable {

    public static final int REQUEST_TIME_CHANGE = 1;
    public static final int DIALOG_EXECUTING = 2;

    public static final int CURRENT_DAY_TIMESPAN = -1;

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");

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

    /**
     * Jumps to the charting activity.
     *
     * @param context         calling intent
     * @param device          concerned device
     * @param graphDefinition series descriptions each representing one series in the resulting chart
     */
    @SuppressWarnings("unchecked")
    public static void showChart(Context context, FhemDevice device, SvgGraphDefinition graphDefinition) {

        context.startActivity(new Intent(context, ChartingActivity.class)
                .putExtra(DEVICE_NAME, device.getName())
                .putExtra(DEVICE_GRAPH_DEFINITION, graphDefinition));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((AndFHEMApplication) getApplication()).inject(this);

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

        svgGraphDefinition = (SvgGraphDefinition) extras.getSerializable(DEVICE_GRAPH_DEFINITION);


        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        String title = extras.getString(ChartFactory.TITLE);
        if (title != null) {
            getSupportActionBar().setTitle(title);
        }

        update(false);
    }

    private int getChartingDefaultTimespan() {
        String timeSpan = PreferenceManager.getDefaultSharedPreferences(this).getString("GRAPH_DEFAULT_TIMESPAN", "24");
        return Integer.valueOf(timeSpan.trim());
    }

    @Override
    public void update(final boolean doUpdate) {
        Intent intent = new Intent(Actions.GET_DEVICE_FOR_NAME);
        intent.setClass(this, RoomListIntentService.class);
        intent.putExtra(DEVICE_NAME, deviceName);
        intent.putExtra(DO_REFRESH, doUpdate);
        intent.putExtra(RESULT_RECEIVER, new FhemResultReceiver() {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != ResultCodes.SUCCESS) return;

                readDataAndCreateChart(doUpdate, (FhemDevice) resultData.getSerializable(DEVICE));
            }
        });
        startService(intent);
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

        List<YAxis> yAxisList = handleChartData(graphData);
        if (graphData.size() == 0) {
            setContentView(R.layout.no_graph_entries);
            return;
        }

        XYMultipleSeriesRenderer renderer = buildAndFillRenderer(yAxisList);
        XYMultipleSeriesDataset dataSet = createChartDataSet(yAxisList);

        String title;
        if (DisplayUtil.getWidthInDP() < 500) {
            title = device.getAliasOrName() + "\n\r" +
                    DATE_TIME_FORMATTER.print(startDate) + " - " + DATE_TIME_FORMATTER.print(endDate);
            renderer.setMargins(new int[]{(int) dpToPx(50), (int) dpToPx(18), (int) dpToPx(20), (int) dpToPx(18)});
        } else {
            title = device.getAliasOrName() + " " +
                    DATE_TIME_FORMATTER.print(startDate) + " - " + DATE_TIME_FORMATTER.print(endDate);
            renderer.setMargins(new int[]{(int) dpToPx(30), (int) dpToPx(18), (int) dpToPx(20), (int) dpToPx(18)});
        }
        getSupportActionBar().setTitle(title);

        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.chart, null);
        final LinearLayout chartLayout = (LinearLayout) view.findViewById(R.id.chart);
        final GraphicalView timeChartView = ChartFactory.getTimeChartView(this, dataSet, renderer, "MM-dd HH:mm");
        chartLayout.addView(timeChartView);

        ImageButton zoomOutButton = (ImageButton) view.findViewById(R.id.zoomOut);
        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeChartView.zoomOut();
            }
        });

        ImageButton zoomInButton = (ImageButton) view.findViewById(R.id.zoomIn);
        zoomInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeChartView.zoomIn();
            }
        });

        ImageButton zoomResetButton = (ImageButton) view.findViewById(R.id.zoomReset);
        zoomResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeChartView.zoomReset();
            }
        });

        setContentView(view);
    }

    private List<YAxis> handleChartData(Map<GPlotSeries, List<GraphEntry>> data) {
        removeChartSeriesWithTooFewEntries(data);
        return mapToYAxis(data);
    }

    /**
     * Builds the {@link XYMultipleSeriesRenderer}. This one is responsible for rendering all the afterwards given graph data
     * to a charting pane. What we do here is set all the required options on the main renderer as well as on all the
     * sub renderers responsible for rendering each chart.
     * This also includes setting the min / max value for zooming and panning and the different graph style for
     * regression and sum charts.
     *
     * @param yAxisList list of {@link YAxis} to render
     * @return renderer
     */
    private XYMultipleSeriesRenderer buildAndFillRenderer(List<YAxis> yAxisList) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(yAxisList.size());
        setRendererDefaults(renderer);

        DateTime minDate = null;
        DateTime maxDate = null;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for (int axisNumber = 0; axisNumber < yAxisList.size(); axisNumber++) {
            YAxis yAxis = yAxisList.get(axisNumber);

            DateTime minimumX = yAxis.getMinimumX();
            if (minimumX != null && (minDate == null || minimumX.isBefore(minDate))) {
                minDate = minimumX;
            }

            DateTime maximumX = yAxis.getMaximumX();
            if (maximumX != null && (maxDate == null || maximumX.isAfter(maxDate))) {
                maxDate = maximumX;
            }

            if (yAxis.getMinimumY() < minY) {
                minY = yAxis.getMinimumY();
            }

            if (yAxis.getMaximumY() > maxY) {
                maxY = yAxis.getMaximumY();
            }

            renderer.setYAxisMax(yAxis.getMaximumY(), axisNumber);
            renderer.setYAxisMin(yAxis.getMinimumY(), axisNumber);
            setYAxisDescription(renderer, axisNumber, yAxis);

            for (ChartData chartSeries : yAxis.getCharts()) {
                XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();

                seriesRenderer.setFillPoints(false);

                GPlotSeries plotSeries = chartSeries.getPlotSeries();
                seriesRenderer.setColor(plotSeries.getColor().getHexColor());
                seriesRenderer.setPointStyle(PointStyle.POINT);
                seriesRenderer.setShowLegendItem(true);
                seriesRenderer.setLineWidth(plotSeries.getLineWidth());
                seriesRenderer.setStroke(BasicStroke.SOLID);

                switch (plotSeries.getSeriesType()) {
                    case FILL:
                        seriesRenderer.addFillOutsideLine(new FillOutsideLine(FillOutsideLine.Type.BELOW));
                        break;
                    case DOT:
                        seriesRenderer.setStroke(BasicStroke.DOTTED);
                        break;
                }
                switch (plotSeries.getLineType()) {
                    case POINTS:
                        seriesRenderer.setStroke(BasicStroke.DOTTED);
                        break;
                }

                renderer.addSeriesRenderer(seriesRenderer);
            }
        }

        if (minDate == null || maxDate == null) {
            throw new IllegalArgumentException();
        }

        minY -= 1;
        maxY += 1;

        renderer.setPanLimits(new double[]{minDate.getMillis(), maxDate.getMillis(), Double.MIN_VALUE, Double.MAX_VALUE});
        renderer.setZoomLimits(new double[]{minDate.getMillis(), maxDate.getMillis(), minY, maxY});

        return renderer;
    }

    /**
     * Create the actual data set for the graph. This means mapping the domain model, especially the amount of
     * contained {@link YAxis} and its {@link ChartData} to the internal {@link CustomTimeSeries}.
     *
     * @param yAxisList list of {@link YAxis} to be mapped on the current data set
     * @return data set in AChartEngine's format
     */
    private XYMultipleSeriesDataset createChartDataSet(List<YAxis> yAxisList) {
        XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();

        for (int yAxisIndex = 0; yAxisIndex < yAxisList.size(); yAxisIndex++) {
            YAxis yAxis = yAxisList.get(yAxisIndex);

            for (ChartData chartData : yAxis.getCharts()) {
                CustomTimeSeries timeSeries = new CustomTimeSeries(chartData.getPlotSeries().getTitle(), yAxisIndex);
                for (GraphEntry graphEntry : chartData.getGraphData()) {
                    timeSeries.add(graphEntry.getDate().toDate(), graphEntry.getValue());
                }
                dataSet.addSeries(timeSeries);
            }

        }
        return dataSet;
    }

    /**
     * Remove all graph series with less than 2 entries.
     *
     * @param data data to work on (which is also modified here)
     */
    private void removeChartSeriesWithTooFewEntries(Map<GPlotSeries, List<GraphEntry>> data) {
        for (GPlotSeries chartSeriesDescription : newHashSet(data.keySet())) {
            if (data.get(chartSeriesDescription).size() < 2) {
                data.remove(chartSeriesDescription);
            }
        }
    }

    /**
     * Maps the amount of chart description to the internal domain model
     *
     * @param data amount of data
     * @return internal representation
     */
    private List<YAxis> mapToYAxis(Map<GPlotSeries, List<GraphEntry>> data) {
        GPlotDefinition plotDefinition = svgGraphDefinition.getPlotDefinition();

        return ImmutableList.of(createAxisFrom(data, plotDefinition.getLeftAxis()),
                createAxisFrom(data, plotDefinition.getRightAxis()));
    }

    private YAxis createAxisFrom(Map<GPlotSeries, List<GraphEntry>> data, GPlotAxis axisDef) {
        YAxis axis = new YAxis(this, axisDef, svgGraphDefinition.formatText(axisDef.getLabel()));
        for (GPlotSeries series : axisDef.getSeries()) {
            if (data.containsKey(series)) {
                axis.addChart(series, data.get(series));
            }
        }
        axis.afterSeriesSet();
        return axis;
    }

    /**
     * Sets the renderer defaults. Nothing special here.
     *
     * @param renderer renderer
     */
    private void setRendererDefaults(XYMultipleSeriesRenderer renderer) {
        renderer.setPointSize(5f);
        renderer.setMargins(new int[]{20, 30, 15, 20});
        renderer.setShowGrid(true);
        renderer.setXLabelsAlign(Paint.Align.CENTER);
        renderer.setZoomButtonsVisible(false);
        renderer.setExternalZoomEnabled(true);
        renderer.setXTitle(getResources().getString(R.string.time));
        renderer.setChartTitle("");
        renderer.setAxesColor(Color.WHITE);
        renderer.setLabelsColor(Color.WHITE);
        renderer.setAxisTitleTextSize(dpToPx(14));
        renderer.setChartTitleTextSize(0);
        renderer.setLabelsTextSize(dpToPx(10));
        renderer.setLegendTextSize(dpToPx(14));
    }

    /**
     * Set the {@link YAxis} description. This includes the axis description style (color, position) as well as the title
     * itself.
     *
     * @param renderer   Renderer to set the values on.
     * @param axisNumber axis number.
     * @param yAxis      Axis to set
     */
    private void setYAxisDescription(XYMultipleSeriesRenderer renderer, int axisNumber, YAxis yAxis) {
        String title = yAxis.getLabel();
        renderer.setYTitle(title, axisNumber);

        if (axisNumber == 0) {
            renderer.setYAxisAlign(Paint.Align.LEFT, 0);
            renderer.setYLabelsAlign(Paint.Align.LEFT, 0);
        } else {
            renderer.setYAxisAlign(Paint.Align.RIGHT, 1);
            renderer.setYLabelsAlign(Paint.Align.RIGHT, 1);
        }

        renderer.setYLabelsColor(axisNumber, getResources().getColor(android.R.color.white));
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
                Intent intent = new Intent(this, ChartingDateSelectionActivity.class);
                intent.putExtras(new Bundle());
                intent.putExtra(DEVICE_NAME, deviceName);
                intent.putExtra(START_DATE, startDate);
                intent.putExtra(END_DATE, endDate);
                startActivityForResult(intent, REQUEST_TIME_CHANGE);
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
