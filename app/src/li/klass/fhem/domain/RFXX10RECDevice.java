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

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.genericview.DetailOverviewViewSettings;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;

import org.w3c.dom.NamedNodeMap;

@DetailOverviewViewSettings(showState = true)
@FloorplanViewSettings(showState = true)
@SuppressWarnings("unused")
public class RFXX10RECDevice extends Device<RFXX10RECDevice> {
    @ShowField(description = ResourceIdMapper.lastStateChange, showInOverview = true)
    private String lastStateChangeTime;

    @ShowField(description = ResourceIdMapper.lastState, showInOverview = true)
    private String lastState;

    public void readTIME(String value)  {
        measured = value;
    }

    public void readSTATECHANGE(String value, NamedNodeMap attributes)  {
        lastStateChangeTime = attributes.getNamedItem("measured").getNodeValue();
        lastState = value;
    }

    public String getLastStateChangedTime() {
        return lastStateChangeTime;
    }

    public String getLastState() {
        return lastState;
    }
}
