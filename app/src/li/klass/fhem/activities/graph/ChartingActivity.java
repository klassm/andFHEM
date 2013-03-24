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

import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
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
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.SimpleDateFormat;
import java.util.*;

import static li.klass.fhem.constants.BundleExtraKeys.*;
import static li.klass.fhem.util.DisplayUtil.dpToPx;

/**
 * Shows a chart.
 */
public class ChartingActivity extends Activity implements Updateable {

    public static final int[] AVAILABLE_COLORS = new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.GRAY};

    private class ScaleMappingKey {
        private int scaleNumber;
        private String yAxisName;

        private ScaleMappingKey(int scaleNumber, String yAxisName) {
            this.scaleNumber = scaleNumber;
            this.yAxisName = yAxisName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ScaleMappingKey that = (ScaleMappingKey) o;

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

    private static final int OPTION_CHANGE_DATA = 1;
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

        String title = extras.getString(ChartFactory.TITLE);
        if (title == null) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        } else if (title.length() > 0) {
            setTitle(title);
        }

        update(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, OPTION_CHANGE_DATA, 0, R.string.optionChangeStartEndDate);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        int itemId = item.getItemId();
        switch (itemId) {
            case OPTION_CHANGE_DATA:
                Intent intent = new Intent(this, ChartingDateSelectionActivity.class);
                intent.putExtras(new Bundle());
                intent.putExtra(DEVICE_NAME, deviceName);
                intent.putExtra(START_DATE, startDate.getTime());
                intent.putExtra(END_DATE, endDate.getTime());
                startActivityForResult(intent, REQUEST_TIME_CHANGE);
                return true;
        }

        return false;
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

        for (ChartSeriesDescription chartSeriesDescription : new HashSet<ChartSeriesDescription>(graphData.keySet())) {
            if (graphData.get(chartSeriesDescription).size() < 2) {
                graphData.remove(chartSeriesDescription);
            }
        }

        if (graphData.size() == 0) {
            setContentView(R.layout.no_graph_entries);
            return;
        }

        Map<ScaleMappingKey, List<ChartSeriesDescription>> scaleMapping = createScaleMapping(graphData);

        XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();

        List<SeriesContainer> seriesList = new ArrayList<SeriesContainer>();

        for (ScaleMappingKey scaleMappingKey : scaleMapping.keySet()) {
            List<SeriesContainer> series = createChart(dataSet, scaleMappingKey.scaleNumber, scaleMapping.get(scaleMappingKey), graphData);
            seriesList.addAll(series);
        }


        double yMin = 1000;
        double yMax = -1000;

        double xMin = System.currentTimeMillis();
        double xMax = 0;


        for (SeriesContainer seriesContainer : seriesList) {
            XYSeries series = seriesContainer.timeSeries;
            double seriesYMax = series.getMaxY();

            if (seriesYMax > yMax) yMax = seriesYMax;

            double seriesYMin = series.getMinY();
            if (seriesYMin < yMin) yMin = seriesYMin;

            if (series.getMinX() < xMin) xMin = series.getMinX();
            if (series.getMaxX() > xMax) xMax = series.getMaxX();
        }

        double yMaxAbsolute = absolute(yMax);
        double yOffset = yMaxAbsolute * 0.1;


        XYMultipleSeriesRenderer renderer = buildRenderer(scaleMapping.size(), seriesList.size(), PointStyle.CIRCLE);

        for (int i = 0; i < renderer.getSeriesRendererCount(); i++) {
            XYSeriesRenderer seriesRenderer = (XYSeriesRenderer) renderer.getSeriesRendererAt(i);

            seriesRenderer.setFillPoints(false);
            seriesRenderer.setColor(AVAILABLE_COLORS[i]);
            seriesRenderer.setPointStyle(PointStyle.POINT);

            SeriesContainer seriesContainer = seriesList.get(i);
            switch (seriesContainer.seriesType) {
                case REGRESSION:
                    seriesRenderer.setLineWidth(1);
                    break;
                case SUM:
                    seriesRenderer.setFillBelowLine(true);
                    break;
                default:
                    seriesRenderer.setLineWidth(2);
            }
        }

        String xTitle = getResources().getString(R.string.time);

        renderer.setShowGrid(true);
        renderer.setXLabelsAlign(Paint.Align.CENTER);

        renderer.setPanLimits(new double[]{xMin, xMax, yMin, yMax});
        renderer.setZoomLimits(new double[]{xMin, xMax, yMin, yMax});
        renderer.setZoomButtonsVisible(false);

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
        setChartSettings(renderer, title, xTitle, yMin - yOffset, yMax + yOffset,
                Color.LTGRAY, Color.LTGRAY);


        renderer.setAxisTitleTextSize(dpToPx(14));
        renderer.setChartTitleTextSize(dpToPx(18));
        renderer.setLabelsTextSize(dpToPx(10));
        renderer.setLegendTextSize(dpToPx(14));


        setYAxisTitles(scaleMapping, renderer);

        GraphicalView timeChartView = ChartFactory.getTimeChartView(this, dataSet, renderer, "MM-dd HH:mm");
        setContentView(timeChartView);
    }

    private List<SeriesContainer> createChart(XYMultipleSeriesDataset dataSet, int scaleNumber, List<ChartSeriesDescription> chartSeriesDescriptions, Map<ChartSeriesDescription, List<GraphEntry>> graphData) {

        List<SeriesContainer> resultList = new ArrayList<SeriesContainer>();

        Collections.sort(chartSeriesDescriptions, new Comparator<ChartSeriesDescription>() {
            @Override
            public int compare(ChartSeriesDescription seriesDescription, ChartSeriesDescription otherSeriesDescription) {
                return ((Boolean) seriesDescription.isShowDiscreteValues()).compareTo(otherSeriesDescription.isShowDiscreteValues());
            }
        });

        Date xMin = new Date();
        Date xMax = new Date(0L);

        // add values as data series, find out max and min values
        for (ChartSeriesDescription chartSeriesDescription : chartSeriesDescriptions) {
            String dataSetName = chartSeriesDescription.getColumnName();
            List<GraphEntry> data = graphData.get(chartSeriesDescription);

            TimeSeries timeSeries = new CustomTimeSeries(dataSetName, scaleNumber);
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

            resultList.add(new SeriesContainer(scaleNumber, timeSeries, SeriesType.DEFAULT));
            dataSet.addSeries(timeSeries);
        }

        // render regression and sum series
        for (ChartSeriesDescription seriesDescription : chartSeriesDescriptions) {
            String dataSetName = seriesDescription.getColumnName();
            List<GraphEntry> data = graphData.get(seriesDescription);

            if (seriesDescription.isShowRegression()) {
                TimeSeries regressionSeries = new CustomTimeSeries(getResources().getString(R.string.regression) + " " + dataSetName, scaleNumber);
                createRegressionForSeries(regressionSeries, data);

                resultList.add(new SeriesContainer(scaleNumber, regressionSeries, SeriesType.REGRESSION));
                dataSet.addSeries(regressionSeries);
            }

            if (seriesDescription.isShowSum()) {
                TimeSeries sumSeries = new CustomTimeSeries(getResources().getString(R.string.sum) + " " + dataSetName, scaleNumber);
                createSumForSeries(sumSeries, data, xMin, xMax, seriesDescription.getSumDivisionFactor());

                resultList.add(new SeriesContainer(scaleNumber, sumSeries, SeriesType.SUM));
                dataSet.addSeries(sumSeries);
            }

        }

        return resultList;
    }

    private void setYAxisTitles(Map<ScaleMappingKey, List<ChartSeriesDescription>> scaleMapping, XYMultipleSeriesRenderer renderer) {
        for (ScaleMappingKey scaleMappingKey : scaleMapping.keySet()) {
            int scaleNumber = scaleMappingKey.scaleNumber;

            String title = scaleMappingKey.yAxisName;
            renderer.setYTitle(title, scaleNumber);

            if (scaleNumber == 0) {
                renderer.setYAxisAlign(Paint.Align.LEFT, 0);
                renderer.setYLabelsAlign(Paint.Align.LEFT, 0);
            } else {
                renderer.setYAxisAlign(Paint.Align.RIGHT, 1);
                renderer.setYLabelsAlign(Paint.Align.RIGHT, 1);
            }

            renderer.setYLabelsColor(scaleNumber, getResources().getColor(android.R.color.black));
        }


    }

    private Map<ScaleMappingKey, List<ChartSeriesDescription>> createScaleMapping(Map<ChartSeriesDescription, List<GraphEntry>> graphData) {
        Map<ScaleMappingKey, List<ChartSeriesDescription>> mapping = new HashMap<ScaleMappingKey, List<ChartSeriesDescription>>();

        for (ChartSeriesDescription chartSeriesDescription : graphData.keySet()) {
            List<ChartSeriesDescription> scaleMappingList = getScaleMappingListFor(chartSeriesDescription.getYAxisName(), mapping);
            if (scaleMappingList != null) {
                scaleMappingList.add(chartSeriesDescription);
            } else {
                ScaleMappingKey key = new ScaleMappingKey(mapping.size(), chartSeriesDescription.getYAxisName());
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


    private List<ChartSeriesDescription> getScaleMappingListFor(String yAxisResource, Map<ScaleMappingKey, List<ChartSeriesDescription>> mapping) {
        for (ScaleMappingKey scaleMappingKey : mapping.keySet()) {
            if (scaleMappingKey.yAxisName.equals(yAxisResource)) {
                return mapping.get(scaleMappingKey);
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


    private XYMultipleSeriesRenderer buildRenderer(int scaleNumber, int numberOfSeries, PointStyle pointStyle) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(scaleNumber);
        setRenderer(renderer, numberOfSeries, pointStyle);
        return renderer;
    }

    private void setRenderer(XYMultipleSeriesRenderer renderer, int numberOfSeries, PointStyle pointStyle) {
        renderer.setAxisTitleTextSize(16);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(15);
        renderer.setLegendTextSize(15);
        renderer.setPointSize(5f);
        renderer.setMargins(new int[]{20, 30, 15, 20});
        for (int i = 0; i < numberOfSeries; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setPointStyle(pointStyle);
            renderer.addSeriesRenderer(r);
        }
    }

    private void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
                                  double yMin, double yMax, int axesColor,
                                  int labelsColor) {
        renderer.setChartTitle(title);
        renderer.setXTitle(xTitle);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
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
