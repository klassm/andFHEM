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

package li.klass.fhem.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DimmableDiscreteStatesDevice;
import li.klass.fhem.domain.genericview.DetailOverviewViewSettings;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.NumberSystemUtil;

import org.w3c.dom.NamedNodeMap;

@DetailOverviewViewSettings(showState = true)
@FloorplanViewSettings()
@SuppressWarnings("unused")
public class FS20Device extends DimmableDiscreteStatesDevice<FS20Device> implements Comparable<FS20Device>, Serializable {

    /**
     * List of dim states available for FS20 devices. Careful: this list has to be ordered, to make dim up and
     * down work!
     */
    public static final List<String> dimStates =
            Arrays.asList("off", "dim6%", "dim12%", "dim18%", "dim25%", "dim31%", "dim37%", "dim43%", "dim50%", "dim56%",
                    "dim62%", "dim68%", "dim75%", "dim81%", "dim87%", "dim93%", "on");
    public static final List<String> dimModels = Arrays.asList("FS20DI", "FS20DI10", "FS20DU");
    public static final List<String> offStates = Arrays.asList("off", "off-for-timer", "reset", "timer");

    @ShowField(description = ResourceIdMapper.model)
    private String model;


    public enum FS20State {
        ON, OFF
    }

    public void readMODEL(String value) {
        this.model = value.toUpperCase();
    }

    @Override
    public void readDEF(String value) {
        super.readDEF(value);

        String[] parts = value.split(" ");
        if (parts.length == 2 && parts[0].length() == 4 && parts[1].length() == 2) {
            definition = transformHexTo4System(parts[0]) + " " + transformHexTo4System(parts[1]);
        }
    }

    @Override
    public void readSTATE(String tagName, NamedNodeMap attributes, String value) {
        super.readSTATE(tagName, attributes, value);
        if (tagName.equals("STATE")) {
            measured = attributes.getNamedItem("measured").getNodeValue();
        }
    }

    public boolean isOn() {
        return getFs20State() == FS20State.ON;
    }

    @Override
    public boolean supportsToggle() {
        return true;
    }

    @Override
    public boolean supportsDim() {
        return dimModels.contains(model);
    }

    @Override
    public List<String> getDimStates() {
        return dimStates;
    }

    public FS20State getFs20State() {
        for (String offState : offStates) {
            if (getInternalState().equals(offState) || getInternalState().equals(eventMap.get(offState))) {
                return FS20State.OFF;
            }
        }
        return FS20State.ON;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        addDeviceChartIfNotNull(getState(), new DeviceChart(R.string.stateGraph, R.string.yAxisFS20State,
                new ChartSeriesDescription(R.string.state, "3:::$fld[2]=~/on.*/?1:0", true, false, false, 1)));
    }

    private String transformHexTo4System(String input) {
        return NumberSystemUtil.hexToQuaternary(input, 4);
    }
}
