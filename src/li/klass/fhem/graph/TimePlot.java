package li.klass.fhem.graph;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import li.klass.fhem.R;
import li.klass.fhem.activities.base.BaseActivity;
import li.klass.fhem.data.FHEMService;
import li.klass.fhem.data.provider.graph.GraphEntry;
import li.klass.fhem.domain.Device;
import li.klass.fhem.exception.DeviceListParseException;
import li.klass.fhem.exception.HostConnectionException;
import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TimePlot extends AbstractDemoChart {

    public static final TimePlot INSTANCE = new TimePlot();

    private TimePlot() {
    }

    @Override
    public String getName() {
        return "name";
    }

    @Override
    public String getDescription() {
        return "desc";
    }

    @SuppressWarnings("unchecked")
    public void execute(Context context, Device device, String yTitle, Integer ... columnSpecificationNames) {
        try {
            new LoadAction(context, device, yTitle, columnSpecificationNames).execute();
        } catch (Exception e) {
            Log.e(TimePlot.class.getName(), e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Intent createChart(Context context, Device device, String yTitle, Integer[] columnSpecificationIds) {
        if (device == null) return null;

        Map<Integer, String> fileLogColumns = device.getFileLogColumns();
        ArrayList<String> columnList = new ArrayList<String>();
        for (Integer columnSpecificationStringsId : columnSpecificationIds) {
            columnList.add(fileLogColumns.get(columnSpecificationStringsId));
        }

        XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();

        Map<String, List<GraphEntry>> graphData = FHEMService.INSTANCE.getGraphData(device, columnList);

        Date xMin = new Date();
        Date xMax = null;

        double yMin = 1000;
        double yMax = -1000;

        for (String columnSpecification : graphData.keySet()) {

            Integer dataSetNameId = findKeyForValue(fileLogColumns, columnSpecification);
            String dataSetName = context.getResources().getString(dataSetNameId);

            TimeSeries seriesName = new TimeSeries(dataSetName);
            List<GraphEntry> data = graphData.get(columnSpecification);

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

        int[] availableColors = new int[] { Color.RED, Color.BLUE, Color.CYAN, Color.GREEN, Color.GRAY};

        XYMultipleSeriesRenderer renderer = buildRenderer(graphData.size(), PointStyle.CIRCLE);
        int length = renderer.getSeriesRendererCount();
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer seriesRenderer = (XYSeriesRenderer) renderer.getSeriesRendererAt(i);
            seriesRenderer.setFillPoints(false);
            seriesRenderer.setColor(availableColors[i]);
            seriesRenderer.setPointStyle(PointStyle.POINT);
        }

        String title = device.getName();
        String xTitle = context.getResources().getString(R.string.time);

        setChartSettings(renderer, title, xTitle, yTitle, xMin.getTime(), xMax.getTime(), yMin - 5, yMax + 5,
                Color.LTGRAY, Color.LTGRAY);
        renderer.setShowGrid(true);
        renderer.setXLabelsAlign(Paint.Align.CENTER);
        renderer.setYLabelsAlign(Paint.Align.CENTER);
        renderer.setZoomButtonsVisible(true);
        renderer.setPanLimits(new double[] { xMin.getTime(), xMax.getTime(), yMin, yMax });
        renderer.setZoomLimits(new double[] { xMin.getTime(), xMax.getTime(), yMin, yMax });

        return ChartFactory.getTimeChartIntent(context, dataSet, renderer, "HH:mm");
    }

    private Integer findKeyForValue(Map<Integer, String> map, String value) {
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    class LoadAction extends AsyncTask<Void, Void, Intent> {

        private volatile Throwable occurredException = null;
        private Context context;
        private Device device;
        private String yTitle;
        private Integer[] columnSpecificationNames;
        private ProgressDialog progressDialog;

        public LoadAction(Context context, Device device, String yTitle, Integer[] columnSpecificationNames) {
            this.context = context;
            this.device = device;
            this.yTitle = yTitle;
            this.columnSpecificationNames = columnSpecificationNames;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, "", context.getResources().getString(R.string.loading));
        }

        @Override
        protected Intent doInBackground(Void... voids) {
            try {
                return createChart(context, device, yTitle, columnSpecificationNames);
            } catch (Exception e) {
                occurredException = e;
                Log.e(BaseActivity.class.getName(), "an error occurred while updating", e);

                return null;
            }
        }

        @Override
        protected void onPostExecute(Intent result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            if (occurredException != null) {
                int messageId = R.string.updateError;
                if (occurredException instanceof HostConnectionException) {
                    messageId = R.string.updateErrorHostConnection;
                } else if (occurredException instanceof DeviceListParseException) {
                    messageId = R.string.updateErrorDeviceListParse;
                }
                Toast.makeText(context, messageId, Toast.LENGTH_LONG).show();
            } else {
                context.startActivity(result);
            }
        }
    }
}
