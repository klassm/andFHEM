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
import li.klass.fhem.R;
import li.klass.fhem.activities.core.Updateable;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.graph.description.SeriesType;
import li.klass.fhem.util.DisplayUtil;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.SimpleDateFormat;
import java.util.*;

import static li.klass.fhem.constants.BundleExtraKeys.*;
import static li.klass.fhem.util.DisplayUtil.dpToPx;

/**
 * Shows a chart.
 */
public class ChartingActivity extends SherlockActivity implements Updateable {

    public static final int[] AVAILABLE_COLORS = new int[]{Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN, Color.GRAY, Color.WHITE};
    public static final Comparator<ChartSeriesDescription> Y_AXIS_NAME_COMPARATOR = new Comparator<ChartSeriesDescription>() {
        @Override
        public int compare(ChartSeriesDescription chartSeriesDescription, ChartSeriesDescription chartSeriesDescription2) {
            return -1 * chartSeriesDescription.getYAxisName().compareTo(chartSeriesDescription2.getYAxisName());
        }
    };

    private class AxisMappingKey implements Comparable<AxisMappingKey> {
        private int scaleNumber;
        private String yAxisName;

        private AxisMappingKey(int scaleNumber, String yAxisName) {
            this.scaleNumber = scaleNumber;
            this.yAxisName = yAxisName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AxisMappingKey that = (AxisMappingKey) o;

            if (scaleNumber != that.scaleNumber) return false;
            if (!yAxisName.equals(that.yAxisName)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = scaleNumber;
            result = 31 * result + (yAxisName != null ? yAxisName.hashCode() : 0);
            return result;
        }

        @Override
        public int compareTo(AxisMappingKey axisMappingKey) {
            return yAxisName.compareTo(axisMappingKey.yAxisName);
        }
    }

    private class SeriesContainer {
        int scaleNumber;
        private TimeSeries timeSeries;
        private SeriesType seriesType;

        private SeriesContainer(int scaleNumber, TimeSeries timeSeries, SeriesType seriesType) {
            this.scaleNumber = scaleNumber;
            this.timeSeries = timeSeries;
            this.seriesType = seriesType;
        }
    }

    public static final int REQUEST_TIME_CHANGE = 1;
    public static final int DIALOG_EXECUTING = 2;

    private String deviceName;

    private ArrayList<ChartSeriesDescription> seriesDescriptions = new ArrayList<ChartSeriesDescription>();

    private Calendar startDate = Calendar.getInstance();
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
        if (graphData.size() == 0) {
            setContentView(R.layout.no_graph_entries);
            return;
        }

        List<YAxis> yAxisList = handleChartData(graphData);
        XYMultipleSeriesRenderer renderer = buildAndFillRenderer(yAxisList);
        XYMultipleSeriesDataset dataSet = createChartDataSet(yAxisList);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String title;
        if (DisplayUtil.getWidthInDP(this) < 500) {
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

    private XYMultipleSeriesRenderer buildAndFillRenderer(List<YAxis> yAxisList) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(yAxisList.size());
        setRendererDefaults(renderer);

        Date minDate = null;
        Date maxDate = null;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

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
                seriesRenderer.setPointStyle(PointStyle.CIRCLE);
                renderer.addSeriesRenderer(seriesRenderer);

                seriesRenderer.setFillPoints(false);
                int color = AVAILABLE_COLORS[i];
                seriesRenderer.setColor(color);
                seriesRenderer.setPointStyle(PointStyle.POINT);

                switch (chartSeries.getChartType()) {
                    case REGRESSION:
                        seriesRenderer.setLineWidth(1);
                        break;
                    case SUM:
                        XYSeriesRenderer.FillOutsideLine fillOutsideLine = new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BELOW);
                        fillOutsideLine.setColor(color);
                        seriesRenderer.addFillOutsideLine(fillOutsideLine);
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


    private List<YAxis> handleChartData(Map<ChartSeriesDescription, List<GraphEntry>> data) {
        removeChartSeriesWithTooFewEntries(data);
        return mapToYAxis(data);
    }

    private void removeChartSeriesWithTooFewEntries(Map<ChartSeriesDescription, List<GraphEntry>> data) {
        for (ChartSeriesDescription chartSeriesDescription : new HashSet<ChartSeriesDescription>(data.keySet())) {
            if (data.get(chartSeriesDescription).size() < 2) {
                data.remove(chartSeriesDescription);
            }
        }
    }

    private List<YAxis> mapToYAxis(Map<ChartSeriesDescription, List<GraphEntry>> data) {
        Map<String, YAxis> yAxisMap = new HashMap<String, YAxis>();

        for (ChartSeriesDescription chartSeriesDescription : data.keySet()) {
            String yAxisName = chartSeriesDescription.getYAxisName();
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

    private int calculateTotalNumberOfSeries(List<YAxis> yAxisList) {
        int total = 0;
        for (YAxis yAxis : yAxisList) {
            total += yAxis.getTotalNumberOfSeries();
        }

        return total;
    }

    private List<SeriesContainer> createChart(XYMultipleSeriesDataset dataSet, int axisNumber,
                                              List<ChartSeriesDescription> chartSeriesDescriptions,
                                              Map<ChartSeriesDescription, List<GraphEntry>> graphData) {

        List<SeriesContainer> resultList = new ArrayList<SeriesContainer>();

        Collections.sort(chartSeriesDescriptions, new Comparator<ChartSeriesDescription>() {
            @Override
            public int compare(ChartSeriesDescription seriesDescription, ChartSeriesDescription otherSeriesDescription) {
                String myName = seriesDescription.getColumnName();
                String otherName = otherSeriesDescription.getColumnName();

                if (!myName.equals(otherName)) return myName.compareTo(otherName);
                return ((Boolean) seriesDescription.isShowDiscreteValues()).compareTo(otherSeriesDescription.isShowDiscreteValues());
            }
        });

        Date xMin = new Date();
        Date xMax = new Date(0L);

        // add values as data series, find out max and min values
        for (ChartSeriesDescription chartSeriesDescription : chartSeriesDescriptions) {
            String dataSetName = chartSeriesDescription.getColumnName();
            List<GraphEntry> data = graphData.get(chartSeriesDescription);

            TimeSeries timeSeries = new CustomTimeSeries(dataSetName, axisNumber);
            float previousValue = -1;

            for (GraphEntry entry : data) {
                Date date = entry.getDate();
                float value = entry.getValue();

                if (previousValue == -1) {
                    previousValue = value;
                }

                if (date == null) continue;

                if ((xMin.after(date))) xMin = date;
                if ((xMax.before(date))) xMax = date;

                if (chartSeriesDescription.isShowDiscreteValues()) {
                    timeSeries.add(new Date(date.getTime() - 1), previousValue);
                    timeSeries.add(date, value);
                    timeSeries.add(new Date(date.getTime() + 1), value);
                } else {
                    timeSeries.add(date, value);
                }

                previousValue = value;
            }

            resultList.add(new SeriesContainer(axisNumber, timeSeries, SeriesType.DEFAULT));
            dataSet.addSeries(timeSeries);
        }

        // render regression and sum series
        for (ChartSeriesDescription seriesDescription : chartSeriesDescriptions) {
            String dataSetName = seriesDescription.getColumnName();
            List<GraphEntry> data = graphData.get(seriesDescription);

            if (seriesDescription.isShowRegression()) {
                TimeSeries regressionSeries = new CustomTimeSeries(getResources().getString(R.string.regression) + " " + dataSetName, axisNumber);
                createRegressionForSeries(regressionSeries, data);

                resultList.add(new SeriesContainer(axisNumber, regressionSeries, SeriesType.REGRESSION));
                dataSet.addSeries(regressionSeries);
            }

            if (seriesDescription.isShowSum()) {
                TimeSeries sumSeries = new CustomTimeSeries(getResources().getString(R.string.sum) + " " + dataSetName, axisNumber);
                createSumForSeries(sumSeries, data, xMin, xMax, seriesDescription.getSumDivisionFactor());

                resultList.add(new SeriesContainer(axisNumber, sumSeries, SeriesType.SUM));
                dataSet.addSeries(sumSeries);
            }

        }

        return resultList;
    }

    private void setYAxisTitles(Map<AxisMappingKey, List<ChartSeriesDescription>> scaleMapping, XYMultipleSeriesRenderer renderer) {
        for (AxisMappingKey axisMappingKey : scaleMapping.keySet()) {
            int scaleNumber = axisMappingKey.scaleNumber;

            String title = axisMappingKey.yAxisName;
            renderer.setYTitle(title, scaleNumber);

            if (scaleNumber == 0) {
                renderer.setYAxisAlign(Paint.Align.LEFT, 0);
                renderer.setYLabelsAlign(Paint.Align.LEFT, 0);
            } else {
                renderer.setYAxisAlign(Paint.Align.RIGHT, 1);
                renderer.setYLabelsAlign(Paint.Align.RIGHT, 1);
            }

            renderer.setYLabelsColor(scaleNumber, getResources().getColor(android.R.color.white));
        }


    }

    private Map<AxisMappingKey, List<ChartSeriesDescription>> createAxisMapping(Map<ChartSeriesDescription, List<GraphEntry>> graphData) {
        Map<AxisMappingKey, List<ChartSeriesDescription>> mapping = new HashMap<AxisMappingKey, List<ChartSeriesDescription>>();

        List<ChartSeriesDescription> keys = new ArrayList<ChartSeriesDescription>(graphData.keySet());
        Collections.sort(keys, Y_AXIS_NAME_COMPARATOR);

        for (ChartSeriesDescription chartSeriesDescription : keys) {
            List<ChartSeriesDescription> scaleMappingList = getScaleMappingListFor(chartSeriesDescription.getYAxisName(), mapping);
            if (scaleMappingList != null) {
                scaleMappingList.add(chartSeriesDescription);
            } else {
                AxisMappingKey key = new AxisMappingKey(mapping.size(), chartSeriesDescription.getYAxisName());
                List<ChartSeriesDescription> keyList = new ArrayList<ChartSeriesDescription>();
                keyList.add(chartSeriesDescription);

                mapping.put(key, keyList);
            }
        }

        if (mapping.size() > 2) {
            throw new IllegalArgumentException("more than two y-axis are not supported!");
        }

        return mapping;
    }


    private List<ChartSeriesDescription> getScaleMappingListFor(String yAxisResource, Map<AxisMappingKey, List<ChartSeriesDescription>> mapping) {
        for (AxisMappingKey axisMappingKey : mapping.keySet()) {
            if (axisMappingKey.yAxisName.equals(yAxisResource)) {
                return mapping.get(axisMappingKey);
            }
        }
        return null;
    }


    private double absolute(double val) {
        return val < 0 ? val * -1 : val;
    }

    private void createRegressionForSeries(TimeSeries resultSeries, List<GraphEntry> entries) {
        float xSum = 0;
        float ySum = 0;
        for (GraphEntry entry : entries) {
            xSum += entry.getDate().getTime();
            ySum += entry.getValue();
        }

        float xAvg = xSum / entries.size();
        float yAvg = ySum / entries.size();

        float b1Numerator = 0;
        float b1Denominator = 0;

        for (GraphEntry entry : entries) {
            b1Numerator += (entry.getValue() - yAvg) * (entry.getDate().getTime() - xAvg);
            b1Denominator += Math.pow(entry.getDate().getTime() - xAvg, 2);
        }

        float b1 = b1Numerator / b1Denominator;
        float b0 = yAvg - b1 * xAvg;


        for (GraphEntry entry : entries) {
            float y = b0 + b1 * entry.getDate().getTime();
            resultSeries.add(entry.getDate(), y);
        }
    }

    private void createSumForSeries(TimeSeries resultSeries, List<GraphEntry> entries, Date xMin, Date xMax, double sumDivisionFactor) {
        double hourDiff = (xMax.getTime() - xMin.getTime()) / 1000 / 60 / 60d;
        double divisionFactor = hourDiff * sumDivisionFactor;

        float ySum = 0;
        for (GraphEntry entry : entries) {
            ySum += entry.getValue();
            resultSeries.add(entry.getDate(), ySum / divisionFactor);
        }
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
     * Goes to the charting activity.
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
