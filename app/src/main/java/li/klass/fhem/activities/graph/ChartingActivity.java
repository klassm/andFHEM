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
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.BasicStroke;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import li.klass.fhem.R;
import li.klass.fhem.activities.core.Updateable;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.graph.description.SeriesType;
import li.klass.fhem.util.DisplayUtil;

import static li.klass.fhem.constants.BundleExtraKeys.DEVICE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_GRAPH_ENTRY_MAP;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_GRAPH_SERIES_DESCRIPTIONS;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;
import static li.klass.fhem.constants.BundleExtraKeys.END_DATE;
import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;
import static li.klass.fhem.constants.BundleExtraKeys.START_DATE;
import static li.klass.fhem.util.DisplayUtil.dpToPx;

/**
 * Shows a chart.
 */
public class ChartingActivity extends SherlockActivity implements Updateable {

    public static final List<Integer> AVAILABLE_COLORS = Arrays.asList(Color.YELLOW, Color.CYAN, Color.GRAY, Color.WHITE);

    public static final int REQUEST_TIME_CHANGE = 1;
    public static final int DIALOG_EXECUTING = 2;

    /**
     * Current device graphs are shown for.
     */
    private String deviceName;

    /**
     * {@link ChartSeriesDescription}s to be shown within the current graph.
     */
    private ArrayList<ChartSeriesDescription> seriesDescriptions = new ArrayList<ChartSeriesDescription>();

    /**
     * Start date for the current graph.
     */
    private Calendar startDate = Calendar.getInstance();

    /**
     * End date for the current graph
     */
    private Calendar endDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(START_DATE)) {
            startDate = (Calendar) savedInstanceState.getSerializable(START_DATE);
        } else {
            startDate.add(Calendar.HOUR, getChartingDefaultTimespan() * (-1));
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(END_DATE)) {
            endDate = (Calendar) savedInstanceState.getSerializable(END_DATE);
        }

        Bundle extras = getIntent().getExtras();
        deviceName = extras.getString(DEVICE_NAME);


        seriesDescriptions = extras.getParcelableArrayList(DEVICE_GRAPH_SERIES_DESCRIPTIONS);


        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        String title = extras.getString(ChartFactory.TITLE);
        if (title != null) {
            getSupportActionBar().setTitle(title);
        }

        update(false);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        getSupportMenuInflater().inflate(R.menu.graph_menu, menu);
        return super.onCreatePanelMenu(featureId, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_changeStartEndDate:
                Intent intent = new Intent(this, ChartingDateSelectionActivity.class);
                intent.putExtras(new Bundle());
                intent.putExtra(DEVICE_NAME, deviceName);
                intent.putExtra(START_DATE, startDate.getTime());
                intent.putExtra(END_DATE, endDate.getTime());
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
                    startDate.setTime((Date) bundle.getSerializable(START_DATE));
                    endDate.setTime((Date) bundle.getSerializable(END_DATE));


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
    public void update(final boolean doUpdate) {
        Intent intent = new Intent(Actions.GET_DEVICE_FOR_NAME);
        intent.putExtra(DEVICE_NAME, deviceName);
        intent.putExtra(DO_REFRESH, doUpdate);
        intent.putExtra(RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != ResultCodes.SUCCESS) return;

                Device device = (Device) resultData.getSerializable(DEVICE);
                readDataAndCreateChart(doUpdate, device);
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
    private void readDataAndCreateChart(boolean doRefresh, final Device device) {
        showDialog(DIALOG_EXECUTING);
        Intent intent = new Intent(Actions.DEVICE_GRAPH);
        intent.putExtra(DO_REFRESH, doRefresh);
        intent.putExtra(DEVICE_NAME, deviceName);
        intent.putExtra(START_DATE, startDate);
        intent.putExtra(END_DATE, endDate);
        intent.putExtra(DEVICE_GRAPH_SERIES_DESCRIPTIONS, seriesDescriptions);
        intent.putExtra(RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                if (resultCode == ResultCodes.SUCCESS) {
                    Map<ChartSeriesDescription, List<GraphEntry>> graphData = (Map<ChartSeriesDescription, List<GraphEntry>>) resultData.get(DEVICE_GRAPH_ENTRY_MAP);
                    createChart(device, graphData);
                }

                try {
                    dismissDialog(DIALOG_EXECUTING);
                } catch (Exception e) {
                    Log.e(ChartingActivity.class.getName(), "error while hiding dialog", e);
                }
            }
        });
        startService(intent);
    }

    /**
     * Actually creates the charting view by using the newly read charting data.
     *
     * @param device    concerned device
     * @param graphData used graph data
     */
    @SuppressWarnings("unchecked")
    private void createChart(Device device, Map<ChartSeriesDescription, List<GraphEntry>> graphData) {

        List<YAxis> yAxisList = handleChartData(graphData);
        if (graphData.size() == 0) {
            setContentView(R.layout.no_graph_entries);
            return;
        }

        XYMultipleSeriesRenderer renderer = buildAndFillRenderer(yAxisList);
        XYMultipleSeriesDataset dataSet = createChartDataSet(yAxisList);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String title;
        if (DisplayUtil.getWidthInDP() < 500) {
            title = device.getAliasOrName() + "\n\r" +
                    dateFormat.format(startDate.getTime()) + " - " + dateFormat.format(endDate.getTime());
            renderer.setMargins(new int[]{(int) dpToPx(50), (int) dpToPx(18), (int) dpToPx(20), (int) dpToPx(18)});
        } else {
            title = device.getAliasOrName() + " " +
                    dateFormat.format(startDate.getTime()) + " - " + dateFormat.format(endDate.getTime());
            renderer.setMargins(new int[]{(int) dpToPx(30), (int) dpToPx(18), (int) dpToPx(20), (int) dpToPx(18)});
        }
        getSupportActionBar().setTitle(title);

        GraphicalView timeChartView = ChartFactory.getTimeChartView(this, dataSet, renderer, "MM-dd HH:mm");

        setContentView(timeChartView);
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

            for (ViewableChartSeries seriesContainer : yAxis) {
                CustomTimeSeries timeSeries = new CustomTimeSeries(seriesContainer.getName(), yAxisIndex);
                for (GraphEntry graphEntry : seriesContainer.getData()) {
                    timeSeries.add(graphEntry.getDate(), graphEntry.getValue());
                }
                dataSet.addSeries(timeSeries);
            }

        }
        return dataSet;
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

        Date minDate = null;
        Date maxDate = null;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        List<Integer> availableColors = new ArrayList<Integer>(AVAILABLE_COLORS);

        int i = 0;
        for (int axisNumber = 0; axisNumber < yAxisList.size(); axisNumber++) {
            YAxis yAxis = yAxisList.get(axisNumber);

            if (minDate == null || yAxis.getMinimumX().before(minDate)) {
                minDate = yAxis.getMinimumX();
            }

            if (maxDate == null || yAxis.getMaximumX().after(maxDate)) {
                maxDate = yAxis.getMaximumX();
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

            for (ViewableChartSeries chartSeries : yAxis) {
                XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();

                seriesRenderer.setFillPoints(false);
                int color = getColorFor(chartSeries, availableColors);
                seriesRenderer.setColor(color);
                seriesRenderer.setPointStyle(PointStyle.POINT);

                renderer.addSeriesRenderer(seriesRenderer);

                switch (chartSeries.getChartType()) {
                    case REGRESSION:
                        seriesRenderer.setLineWidth(1);
                        seriesRenderer.setShowLegendItem(false);
                        seriesRenderer.setStroke(BasicStroke.DOTTED);
                        break;
                    case SUM:
                        seriesRenderer.setLineWidth(1);
                        seriesRenderer.setShowLegendItem(false);
                        seriesRenderer.setStroke(BasicStroke.DOTTED);
                        break;
                    default:
                        seriesRenderer.setLineWidth(2);
                }

                i++;
            }
        }

        if (minDate == null || maxDate == null) {
            throw new IllegalArgumentException();
        }

        renderer.setPanLimits(new double[]{minDate.getTime(), maxDate.getTime(), minY, maxY});
        renderer.setZoomLimits(new double[]{minDate.getTime(), maxDate.getTime(), minY, maxY});

        return renderer;
    }

    private int getColorFor(ViewableChartSeries viewableChartSeries, List<Integer> availableColors) {
        SeriesType seriesType = viewableChartSeries.getSeriesType();
        if (seriesType != null) {
            return seriesType.getColor();
        }

        Integer color = availableColors.get(0);
        availableColors.remove(0);

        return color;
    }

    /**
     * Sets the renderer defaults. Nothing special here.
     *
     * @param renderer renderer
     */
    private void setRendererDefaults(XYMultipleSeriesRenderer renderer) {
        renderer.setAxisTitleTextSize(16);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(15);
        renderer.setLegendTextSize(15);
        renderer.setPointSize(5f);
        renderer.setMargins(new int[]{20, 30, 15, 20});
        renderer.setShowGrid(true);
        renderer.setXLabelsAlign(Paint.Align.CENTER);
        renderer.setZoomButtonsVisible(true);
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
        String title = yAxis.getName();
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

    /**
     * Method mapping all given {@link ChartSeriesDescription}s to a common domain model (from {@link YAxis} outgoing).
     * In addition, chart entries with less than a specified number of entries are removed.
     *
     * @param data loaded data for each {@link ChartSeriesDescription}
     * @return list of {@link YAxis} (internal domain model representation)
     */
    private List<YAxis> handleChartData(Map<ChartSeriesDescription, List<GraphEntry>> data) {
        removeChartSeriesWithTooFewEntries(data);
        return mapToYAxis(data);
    }

    /**
     * Remove all graph series with less than 2 entries.
     *
     * @param data data to work on (which is also modified here)
     */
    private void removeChartSeriesWithTooFewEntries(Map<ChartSeriesDescription, List<GraphEntry>> data) {
        for (ChartSeriesDescription chartSeriesDescription : new HashSet<ChartSeriesDescription>(data.keySet())) {
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
    private List<YAxis> mapToYAxis(Map<ChartSeriesDescription, List<GraphEntry>> data) {
        Map<String, YAxis> yAxisMap = new HashMap<String, YAxis>();

        for (ChartSeriesDescription chartSeriesDescription : data.keySet()) {
            SeriesType seriesType = chartSeriesDescription.getSeriesType();
            if (seriesType == null && chartSeriesDescription.getColumnName() == null) {
                throw new IllegalArgumentException("no series type: " + chartSeriesDescription.getColumnName() + " ; device " + deviceName + "");
            }
            String yAxisName;
            if (seriesType == null) {
                yAxisName = chartSeriesDescription.getFallBackYAxisName();
            } else {
                yAxisName = seriesType.getYAxisName();
            }
            if (!yAxisMap.containsKey(yAxisName)) {
                yAxisMap.put(yAxisName, new YAxis(yAxisName));
            }

            yAxisMap.get(yAxisName).addChart(chartSeriesDescription, data.get(chartSeriesDescription));
        }

        ArrayList<YAxis> yAxisList = new ArrayList<YAxis>(yAxisMap.values());
        Collections.sort(yAxisList);

        for (YAxis yAxis : yAxisList) {
            yAxis.afterSeriesSet();
        }

        return yAxisList;
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

    private int getChartingDefaultTimespan() {
        String timeSpan = PreferenceManager.getDefaultSharedPreferences(this).getString("GRAPH_DEFAULT_TIMESPAN", "24");
        return Integer.valueOf(timeSpan.trim());
    }

    /**
     * Jumps to the charting activity.
     *
     * @param context            calling intent
     * @param device             concerned device
     * @param seriesDescriptions series descriptions each representing one series in the resulting chart
     */
    @SuppressWarnings("unchecked")
    public static void showChart(Context context, Device device, ChartSeriesDescription... seriesDescriptions) {

        ArrayList<ChartSeriesDescription> seriesList = new ArrayList<ChartSeriesDescription>(Arrays.asList(seriesDescriptions));
        Intent timeChartIntent = new Intent(context, ChartingActivity.class);
        timeChartIntent.putExtras(new Bundle());
        timeChartIntent.putExtra(DEVICE_NAME, device.getName());
        timeChartIntent.putExtra(DEVICE_GRAPH_SERIES_DESCRIPTIONS, seriesList);

        context.startActivity(timeChartIntent);
    }
}
