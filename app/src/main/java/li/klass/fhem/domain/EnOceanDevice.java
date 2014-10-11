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

import android.util.Log;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.ToggleWidgetView;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.NumberUtil;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

import static li.klass.fhem.util.Equals.ignoreCaseEither;

@SuppressWarnings("unused")
@OverviewViewSettings(showState = true)
public class EnOceanDevice extends DimmableDevice<EnOceanDevice> {

    private String model;
    private String manufacturerId;
    private int shutterPosition;

    @ShowField(showAfter = "state", description = ResourceIdMapper.shutterPosition)
    private String shutterPositionText;

    public enum SubType {
        SWITCH, SENSOR, DIMMER, SHUTTER
    }

    private SubType subType;
    private String gwCmd;

    private static final String TAG = EnOceanDevice.class.getName();

    public void readSUBTYPE(String value) {
        if (value.equalsIgnoreCase("switch")) {
            subType = SubType.SWITCH;
        } else if (value.equalsIgnoreCase("sensor")) {
            subType = SubType.SENSOR;
        } else if (value.equalsIgnoreCase("gateway")) {
            // handled in #afterDeviceXMLRead
        } else {
            Log.e(TAG, "unknown subtype " + value);
            subType = null;
        }
    }

    public void readMODEL(String value) {
        this.model = value;
    }

    public void readMANUFID(String value) {
        this.manufacturerId = value;
    }

    public void readGWCMD(String value) {
        this.gwCmd = value;
    }

    public void readPOSITION(String value) {
        shutterPosition = ValueExtractUtil.extractLeadingInt(value);
        shutterPositionText = ValueDescriptionUtil.appendPercent(value);
    }

    @Override
    public void afterDeviceXMLRead() {
        super.afterDeviceXMLRead();

        if (gwCmd != null) {
            if (gwCmd.equalsIgnoreCase("DIMMING")) {
                subType = SubType.DIMMER;
            } else if (gwCmd.equalsIgnoreCase("SWITCHING")) {
                subType = SubType.SWITCH;
            }
        }

        if (ignoreCaseEither(model, "FSB14", "FSB61", "FSB70")) {
            subType = SubType.SHUTTER;
            readONOFFDEVICE("true");
        }
    }

    @Override
    public boolean supportsToggle() {
        return subType == SubType.SWITCH || subType == SubType.SHUTTER;
    }

    @Override
    public int getDimUpperBound() {
        return 100;
    }

    @Override
    public String getDimStateForPosition(int position) {
        return "dim " + position;
    }

    @Override
    public int getPositionForDimState(String dimState) {
        dimState = dimState.replaceAll("dim", "").replaceAll("%", "").trim();
        if (!NumberUtil.isDecimalNumber(dimState)) {
            return 0;
        }
        return ValueExtractUtil.extractLeadingInt(dimState);
    }

    @Override
    public boolean supportsDim() {
        return subType == SubType.DIMMER;
    }

    public SubType getSubType() {
        return subType;
    }

    @Override
    public boolean supportsWidget(Class<? extends DeviceAppWidgetView> appWidgetClass) {
        if (appWidgetClass.equals(ToggleWidgetView.class) && subType != SubType.SWITCH) {
            return false;
        }

        return super.supportsWidget(appWidgetClass);
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        if (subType == SubType.SHUTTER) return DeviceFunctionality.WINDOW;
        return DeviceFunctionality.functionalityForDimmable(this);
    }

    @Override
    public String getOffStateName() {
        if (subType == SubType.SHUTTER) return "closes";

        if (eventMapReverse.containsKey("off")) {
            return eventMapReverse.get("off");
        }
        return "BI";
    }

    @Override
    public String getOnStateName() {
        if (subType == SubType.SHUTTER) return "opens";

        if (eventMapReverse.containsKey("on")) {
            return eventMapReverse.get("on");
        }
        return "B0";
    }

    public void setShutterPosition(int shutterPosition) {
        this.shutterPosition = shutterPosition;
        this.shutterPositionText = ValueDescriptionUtil.appendPercent(shutterPosition);
    }

    public String getModel() {
        return model;
    }

    public String getManufacturerId() {
        return manufacturerId;
    }

    public int getShutterPosition() {
        return shutterPosition;
    }
}
