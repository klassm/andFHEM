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
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import li.klass.fhem.R;
import li.klass.fhem.activities.base.Updateable;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.Device;
import li.klass.fhem.service.graph.GraphEntry;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Shows a chart.
 */
public class ChartingActivity extends Activity implements Updateable {

    private static final int OPTION_CHANGE_DATA = 1;
    public static final int REQUEST_TIME_CHANGE = 1;

    public static final int DIALOG_EXECUTING = 2;

    private String deviceName;
    private String yTitle;

    private int[] columnSpecificationIds;
    private Calendar startDate = Calendar.getInstance();

    private Calendar endDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        deviceName = extras.getString(BundleExtraKeys.DEVICE_NAME);
        yTitle = extras.getString(BundleExtraKeys.DEVICE_GRAPH_Y_TITLE);
        columnSpecificationIds = extras.getIntArray(BundleExtraKeys.DEVICE_GRAPH_COLUMN_SPECIFICATION_IDS);

        startDate.roll(Calendar.DAY_OF_MONTH, -1);
        endDate.roll(Calendar.DAY_OF_MONTH, 1);

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
                intent.putExtra(ChartingDateSelectionActivity.INTENT_DEVICE_NAME, deviceName);
                intent.putExtra(ChartingDateSelectionActivity.INTENT_START_DATE, startDate.getTime());
                intent.putExtra(ChartingDateSelectionActivity.INTENT_END_DATE, endDate.getTime());
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
                    startDate.setTime((Date) bundle.getSerializable(ChartingDateSelectionActivity.INTENT_START_DATE));
                    endDate.setTime((Date) bundle.getSerializable(ChartingDateSelectionActivity.INTENT_END_DATE));

                    update(false);
            }
        }
    }

    @Override
    public void update(final boolean doUpdate) {
        Intent intent = new Intent(Actions.GET_DEVICE_FOR_NAME);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
        intent.putExtra(BundleExtraKeys.DO_REFRESH, doUpdate);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                Device device = (Device) resultData.getSerializable(BundleExtraKeys.DEVICE);
                readDataAndCreateChart(doUpdate, device);
            }
        });
        startService(intent);
    }

    /**
     * Reads all the charting data for a given date and the column specifications set as attribute.
     * @param doRefresh should the underlying room device list be refreshed?
     * @param device concerned device
     */
    @SuppressWarnings("unchecked")
    private void readDataAndCreateChart(boolean doRefresh, final Device device) {
        showDialog(DIALOG_EXECUTING);
        Intent intent = new Intent(Actions.DEVICE_GRAPH);
        intent.putExtra(BundleExtraKeys.DO_REFRESH, doRefresh);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
        intent.putExtra(BundleExtraKeys.START_DATE, startDate);
        intent.putExtra(BundleExtraKeys.END_DATE, endDate);
        intent.putExtra(BundleExtraKeys.DEVICE_GRAPH_COLUMN_SPECIFICATION_IDS, columnSpecificationIds);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                if (resultCode == ResultCodes.SUCCESS) {
                    Map<Integer, List<GraphEntry>> graphData = (Map<Integer, List<GraphEntry>>) resultData.get(BundleExtraKeys.DEVICE_GRAPH_ENTRY_MAP);
                    createChart(device, graphData);
                }

                dismissDialog(DIALOG_EXECUTING);
            }
        });
        startService(intent);
    }

    /**
     * Actually creates the charting view by using the newly read charting data.
     * @param device concerned device
     * @param graphData used graph data
     */
    @SuppressWarnings("unchecked")
    private void createChart(Device device, Map<Integer, List<GraphEntry>> graphData) {

        XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();

        Date xMin = new Date();
        Date xMax = null;

        double yMin = 1000;
        double yMax = -1000;

        for (Integer dataSetNameId : graphData.keySet()) {

            String dataSetName = getResources().getString(dataSetNameId);

            TimeSeries seriesName = new TimeSeries(dataSetName);
            List<GraphEntry> data = graphData.get(dataSetNameId);

            for (GraphEntry graphEntry : data) {
                Date date = graphEntry.getDate();
                float value = graphEntry.getValue();

                if (date != null && (xMin.after(date))) xMin = date;
                if (date != null && (xMax == null || xMax.before(date))) xMax = date;

                if (yMin > value) yMin = value;
                if (yMax < value) yMax = value;

                seriesName.add(date, value);
            }

            dataSet.addSeries(seriesName);
        }

        if (xMax == null) xMax = new Date();

        int[] availableColors = new int[]{Color.RED, Color.BLUE, Color.CYAN, Color.GREEN, Color.GRAY};

        XYMultipleSeriesRenderer renderer = buildRenderer(graphData.size(), PointStyle.CIRCLE);
        int length = renderer.getSeriesRendererCount();
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer seriesRenderer = (XYSeriesRenderer) renderer.getSeriesRendererAt(i);
            seriesRenderer.setFillPoints(false);
            seriesRenderer.setColor(availableColors[i]);
            seriesRenderer.setPointStyle(PointStyle.POINT);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String title = device.getAliasOrName() + " " +
                dateFormat.format(startDate.getTime()) + " - " + dateFormat.format(endDate.getTime());
        String xTitle = getResources().getString(R.string.time);

        setChartSettings(renderer, title, xTitle, yTitle, xMin.getTime(), xMax.getTime(), yMin - 5, yMax + 5,
                Color.LTGRAY, Color.LTGRAY);
        renderer.setShowGrid(true);
        renderer.setXLabelsAlign(Paint.Align.CENTER);
        renderer.setYLabelsAlign(Paint.Align.CENTER);
        renderer.setZoomButtonsVisible(true);
        renderer.setPanLimits(new double[]{xMin.getTime(), xMax.getTime(), yMin, yMax});
        renderer.setZoomLimits(new double[]{xMin.getTime(), xMax.getTime(), yMin, yMax});

        GraphicalView timeChartView = ChartFactory.getTimeChartView(this, dataSet, renderer, "MM-dd HH:mm");
        setContentView(timeChartView);
    }


    protected XYMultipleSeriesRenderer buildRenderer(int numberOfSeries, PointStyle pointStyle) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        setRenderer(renderer, numberOfSeries, pointStyle);
        return renderer;
    }

    protected void setRenderer(XYMultipleSeriesRenderer renderer, int numberOfSeries, PointStyle pointStyle) {
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

    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
                                    String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
                                    int labelsColor) {
        renderer.setChartTitle(title);
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
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


    /**
     * Goes to the charting activity.
     *
     * @param context                  calling intent
     * @param device                   concerned device
     * @param yTitle                   description of the values (only one!)
     * @param columnSpecificationNames column specifications off all graph series.
     */
    @SuppressWarnings("unchecked")
    public static void showChart(Context context, Device device, String yTitle, int... columnSpecificationNames) {

        Intent timeChartIntent = new Intent(context, ChartingActivity.class);
        timeChartIntent.putExtras(new Bundle());
        timeChartIntent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        timeChartIntent.putExtra(BundleExtraKeys.DEVICE_GRAPH_Y_TITLE, yTitle);
        timeChartIntent.putExtra(BundleExtraKeys.DEVICE_GRAPH_COLUMN_SPECIFICATION_IDS, columnSpecificationNames);

        context.startActivity(timeChartIntent);
    }
}
