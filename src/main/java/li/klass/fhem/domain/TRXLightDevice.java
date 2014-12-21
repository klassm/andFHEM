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

import org.w3c.dom.NamedNodeMap;

import java.util.ArrayList;
import java.util.List;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DimmableDiscreteStatesDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;

@SuppressWarnings("unused")
@OverviewViewSettings(showState = true)
public class TRXLightDevice extends DimmableDiscreteStatesDevice<TRXLightDevice> {

    private static final ArrayList<String> dimLevels = new ArrayList<String>();

    static {
        dimLevels.add("off");
        for (int i = 0; i <= 15; i++) {
            dimLevels.add("level " + i);
        }
    }

    @ShowField(description = ResourceIdMapper.type)
    private String type;

    public void readTRX_LIGHT_TYPE(String value) {
        this.type = value;
    }

    @Override
    public void readSTATE(String tagName, NamedNodeMap attributes, String value) {
        super.readSTATE(tagName, attributes, value);

        if (tagName.equals("STATE")) {
            String measured = attributes.getNamedItem("measured").getNodeValue();
            setMeasured(measured);
        }
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.functionalityForDimmable(this);
    }

    @Override
    public boolean isOnByState() {
        if (super.isOnByState()) return true;

        return !getInternalState().equalsIgnoreCase("off");
    }

    @Override
    public boolean supportsToggle() {
        return true;
    }

    @SuppressWarnings("unused")
    public String getType() {
        return type;
    }

    @Override
    public boolean supportsDim() {
        return getSetList().contains("all_level");
    }

    @Override
    public List<String> getDimStates() {
        return dimLevels;
    }

    @Override
    public int getPositionForDimState(String dimState) {
        if (dimState.equals("on")) {
            dimState = "level 15";
        }
        return super.getPositionForDimState(dimState);
    }
}
