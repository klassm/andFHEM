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

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.DetailViewSettings;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;

@OverviewViewSettings(showMeasured = true)
@DetailViewSettings(showState = false)
public class RoommateDevice extends Device<RoommateDevice> {

    @ShowField(description = ResourceIdMapper.location, showInOverview = true)
    private String location;

    @ShowField(description = ResourceIdMapper.mood, showInOverview = true)
    private String mood;

    public String getLocation() {
        return location;
    }

    @XmllistAttribute("LOCATION")
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public void onChildItemRead(String tagName, String key, String value, NamedNodeMap attributes) {
        if (tagName.equalsIgnoreCase("STATE") && key.equalsIgnoreCase("STATE")) {
            setMeasured(attributes.getNamedItem("measured").getNodeValue());
        }
        super.onChildItemRead(tagName, key, value, attributes);
    }

    public String getMood() {
        return mood;
    }

    @XmllistAttribute("MOOD")
    public void setMood(String mood) {
        this.mood = mood;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.PRESENCE;
    }
}
