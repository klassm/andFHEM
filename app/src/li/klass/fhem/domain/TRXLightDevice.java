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

import li.klass.fhem.R;
import li.klass.fhem.domain.core.DimmableDiscreteStatesDevice;
import li.klass.fhem.domain.genericview.DetailOverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ArrayUtil;
import org.w3c.dom.NamedNodeMap;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@DetailOverviewViewSettings(showState = true)
public class TRXLightDevice extends DimmableDiscreteStatesDevice<TRXLightDevice> {

    private static final ArrayList<String> dimLevels = new ArrayList<String>();

    static {
        dimLevels.add("off");
        for (int i = 1; i <= 15; i++) {
            dimLevels.add("level " + i);
        }
    }

    @ShowField(description = R.string.type)
    private String type;

    public void readTRX_LIGHT_TYPE(String value) {
        this.type = value;
    }

    @Override
    public void readSTATE(String tagName, NamedNodeMap attributes, String value) {

        if (value.equals("level 0")) value = "off";
        if (value.equals("level 15")) value = "on";

        super.readSTATE(tagName, attributes, value);

        if (tagName.equals("STATE")) {
            this.measured = attributes.getNamedItem("measured").getNodeValue();
        }
    }

    @Override
    public boolean isOn() {
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
        return ArrayUtil.contains(getAvailableTargetStates(), "all_level");
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
