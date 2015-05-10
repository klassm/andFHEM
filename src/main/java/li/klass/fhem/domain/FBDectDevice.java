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

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.resources.ResourceIdMapper;

@SuppressWarnings("unused")
@OverviewViewSettings(showState = true, showMeasured = true)
public class FBDectDevice extends ToggleableDevice<FBDectDevice> {
    @ShowField(description = ResourceIdMapper.energy)
    @XmllistAttribute("energy")
    private String energy;

    @ShowField(description = ResourceIdMapper.power, showInOverview = true)
    @XmllistAttribute("power")
    private String power;

    @ShowField(description = ResourceIdMapper.voltage)
    @XmllistAttribute("voltage")
    private String voltage;

    @ShowField(description = ResourceIdMapper.cumulativeUsage)
    @XmllistAttribute("current")
    private String current;


    public String getEnergy() {
        return energy;
    }

    public String getPower() {
        return power;
    }

    public String getVoltage() {
        return voltage;
    }

    public String getCurrent() {
        return current;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.USAGE;
    }

    @Override
    public boolean supportsToggle() {
        return true;
    }
}
