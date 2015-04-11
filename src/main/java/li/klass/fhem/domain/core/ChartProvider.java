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

package li.klass.fhem.domain.core;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.domain.log.LogDevice;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.DeviceConfigurationProvider;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.graph.description.SeriesType;
import li.klass.fhem.service.room.xmllist.DeviceNode;
import li.klass.fhem.service.room.xmllist.XmlListDevice;

import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Lists.newArrayList;

@Singleton
public class ChartProvider {
    @Inject
    DeviceConfigurationProvider deviceConfigurationProvider;

    public List<DeviceChart> chartsFor(FhemDevice device) {
        List<DeviceChart> result = newArrayList();

        try {
            Optional<JSONObject> configuration = deviceConfigurationProvider.configurationFor(device.getXmlListDevice());
            if (!configuration.isPresent()) {
                return result;
            }
            JSONObject config = configuration.get();

            JSONArray charts = config.optJSONArray("charts");
            if (charts == null) {
                return result;
            }

            for (int c = 0; c < charts.length(); c++) {
                Optional<DeviceChart> optChart = handleChart(charts.getJSONObject(c), device);
                if (optChart.isPresent()) {
                    result.add(optChart.get());
                }
            }

            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<DeviceChart> handleChart(JSONObject chart, FhemDevice device) throws JSONException {
        JSONArray series = chart.getJSONArray("series");
        JSONArray required = chart.optJSONArray("required");
        ResourceIdMapper buttonText = ResourceIdMapper.valueOf(chart.getString("buttonText"));

        if (series == null || !mayShow(required, device.getXmlListDevice())) {
            return Optional.absent();
        }

        List<ChartSeriesDescription> descriptions = newArrayList();
        for (int s = 0; s < series.length(); s++) {
            descriptions.add(handleSeriesDescription(series.getJSONObject(s), device));
        }

        return Optional.of(new DeviceChart(buttonText.getId(), toArray(descriptions, ChartSeriesDescription.class)));
    }

    private boolean mayShow(JSONArray required, XmlListDevice xmlListDevice) throws JSONException {
        if (required == null) return true;

        for (int i = 0; i < required.length(); i++) {
            JSONObject req = required.getJSONObject(i);
            String type = req.getString("type");
            String attribute = req.getString("attribute");

            DeviceNode node;
            switch (type) {
                case "state":
                    node = xmlListDevice.getStates().get(attribute);
                    break;
                default:
                    throw new IllegalArgumentException(type + " is not (yet) supported!");
            }

            if (node == null || node.getValue() == null) {
                return false;
            }
        }
        return true;
    }

    private ChartSeriesDescription handleSeriesDescription(JSONObject series, FhemDevice device) throws JSONException {
        ResourceIdMapper columnName = ResourceIdMapper.valueOf(series.getString("columnName"));
        String fileLogSpec = series.getString("fileLogSpec");
        String dbLogSpec = series.getString("dbLogSpec");
        SeriesType seriesType = SeriesType.valueOf(series.getString("seriesType"));
        boolean showRegression = series.optBoolean("showRegression", false);
        JSONObject yAxisMinMax = series.optJSONObject("yAxisMinMax");

        ChartSeriesDescription.Builder builder = new ChartSeriesDescription.Builder()
                .withColumnName(columnName.getId(), AndFHEMApplication.getContext())
                .withFileLogSpec(fileLogSpec)
                .withDbLogSpec(dbLogSpec)
                .withSeriesType(seriesType)
                .withShowRegression(showRegression);

        if (yAxisMinMax != null) {
            LogDevice o = (LogDevice) device.getLogDevices().get(0);
            builder.withYAxisMinMaxValue(o.getYAxisMinMaxValueFor(yAxisMinMax.getString("attribute"), yAxisMinMax.getInt("defaultMin"), yAxisMinMax.getInt("defaultMax")));
        }
        return builder.build();
    }
}
