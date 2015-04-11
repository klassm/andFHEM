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

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.ToggleWidgetView;
import li.klass.fhem.domain.core.ChartProvider;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DimmableContinuousStatesDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.OverviewViewSettings;

import static li.klass.fhem.util.Equals.ignoreCaseEither;

@SuppressWarnings("unused")
@OverviewViewSettings(showState = true)
public class EnOceanDevice extends DimmableContinuousStatesDevice<EnOceanDevice> {
    public enum SubType {
        SWITCH, SENSOR, DIMMER, SHUTTER
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EnOceanDevice.class);

    @XmllistAttribute("model")
    private String model;

    @XmllistAttribute("manufid")
    private String manufacturerId;

    @XmllistAttribute("gwcmd")
    private String gwCmd;

    private SubType subType;

    @XmllistAttribute("subtype")
    public void setSubtype(String value) {
        if (value.equalsIgnoreCase("switch")) {
            subType = SubType.SWITCH;
        } else if (value.equalsIgnoreCase("sensor")) {
            subType = SubType.SENSOR;
        } else {
            LOGGER.error("setSubtype() - unknown subtype {}", value);
            subType = null;
        }
    }

    @Override
    protected String getSetListDimStateAttributeName() {
        if (setList.contains("position")) return "position";
        if (setList.contains("dim")) return "dim";
        return super.getSetListDimStateAttributeName();
    }

    @Override
    public void afterDeviceXMLRead(Context context, ChartProvider chartProvider) {
        super.afterDeviceXMLRead(context, chartProvider);

        if (gwCmd != null) {
            if (gwCmd.equalsIgnoreCase("DIMMING")) {
                subType = SubType.DIMMER;
            } else if (gwCmd.equalsIgnoreCase("SWITCHING")) {
                subType = SubType.SWITCH;
            }
        }

        if (setList.contains("up", "down") || ignoreCaseEither(model, "FSB14", "FSB61", "FSB70")) {
            subType = SubType.SHUTTER;
        }
    }

    @Override
    public boolean supportsToggle() {
        return setList.contains("on", "off") && subType == SubType.SWITCH;
    }

    @Override
    public boolean supportsDim() {
        return subType == SubType.DIMMER || subType == SubType.SHUTTER;
    }

    public SubType getSubType() {
        return subType;
    }

    @Override
    public boolean supportsWidget(Class<? extends DeviceAppWidgetView> appWidgetClass) {
        return !(appWidgetClass.equals(ToggleWidgetView.class) && subType != SubType.SWITCH)
                && super.supportsWidget(appWidgetClass);
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        if (subType == SubType.SHUTTER) {
            return DeviceFunctionality.WINDOW;
        }
        return DeviceFunctionality.functionalityForDimmable(this);
    }

    @Override
    public String getOffStateName() {
        if (subType == SubType.SHUTTER) {
            return "closes";
        }

        if (eventMapReverse.containsKey("off")) {
            return eventMapReverse.get("off");
        }

        return "BI";
    }

    @Override
    public String getOnStateName() {
        if (subType == SubType.SHUTTER) {
            return "opens";
        }

        if (eventMapReverse.containsKey("on")) {
            return eventMapReverse.get("on");
        }

        return "B0";
    }


    public String getModel() {
        return model;
    }

    public String getManufacturerId() {
        return manufacturerId;
    }
}
