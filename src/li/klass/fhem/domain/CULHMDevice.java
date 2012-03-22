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

import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;
import org.w3c.dom.NamedNodeMap;

public class CULHMDevice extends Device<CULHMDevice> {

    public enum SubType {
        DIMMER, SWITCH, HEATING, SMOKE_DETECTOR, THREE_STATE
    }

    private String measured;
    private SubType subType = null;
    private int dimProgress = -1;
    private String desiredTemp;
    private String measuredTemp;
    private String actuator;
    private String humidity;

    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("SUBTYPE")) {
            if (nodeContent.equalsIgnoreCase("DIMMER") || nodeContent.equalsIgnoreCase("BLINDACTUATOR")) {
                subType = SubType.DIMMER;
            } else if (nodeContent.equalsIgnoreCase("SWITCH")) {
                subType = SubType.SWITCH;
            } else if (nodeContent.equalsIgnoreCase("SMOKEDETECTOR")) {
                subType = SubType.SMOKE_DETECTOR;
            } else if (nodeContent.equalsIgnoreCase("THREESTATESENSOR")) {
                subType = SubType.THREE_STATE;
            }
        } else if (keyValue.equals("STATE")) {
            if (nodeContent.endsWith("%")) {
                dimProgress = ValueExtractUtil.extractLeadingInt(nodeContent);
            }
        } else if (keyValue.equals("DESIRED-TEMP")) {
            desiredTemp = ValueDescriptionUtil.appendTemperature(nodeContent);
        } else if (keyValue.equals("MEASURED-TEMP")) {
            measuredTemp = ValueDescriptionUtil.appendTemperature(nodeContent);
        } else if (keyValue.equals("ACTUATOR")) {
            subType = SubType.HEATING;
            actuator = nodeContent;
        } else if (keyValue.equals("CUL_TIME")) {
            measured = nodeContent;
        } else if (keyValue.equals("HUMIDITY")) {
            humidity = ValueDescriptionUtil.appendPercent(nodeContent);
        }
    }

    @Override
    public boolean isSupported() {
        return subType != null;
    }

    public boolean isOn() {
        String internalState = getInternalState();
        return internalState.equalsIgnoreCase("on") || internalState.equalsIgnoreCase("on-for-timer");
    }

    public int getDimProgress() {
        if (dimProgress == -1) {
            return isOn() ? 100 : 0;
        }
        return dimProgress;
    }

    public void setDimProgress(int dimProgress) {
        this.dimProgress = dimProgress;
    }

    public String getMeasured() {
        return measured;
    }

    public String getDesiredTemp() {
        return desiredTemp;
    }

    public String getMeasuredTemp() {
        return measuredTemp;
    }

    public String getActuator() {
        return actuator;
    }

    public SubType getSubType() {
        return subType;
    }

    public String getHumidity() {
        return humidity;
    }
}
