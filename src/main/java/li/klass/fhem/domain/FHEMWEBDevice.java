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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.resources.ResourceIdMapper;

import static li.klass.fhem.domain.core.DeviceFunctionality.FHEM;

public class FHEMWEBDevice extends FhemDevice {

    @ShowField(description = ResourceIdMapper.hiddenRooms)
    @XmllistAttribute("HIDDENROOM")
    private String hiddenRoom;

    @ShowField(description = ResourceIdMapper.hiddenGroups)
    @XmllistAttribute("HIDDENGROUP")
    private String hiddenGroup;

    @ShowField(description = ResourceIdMapper.sortRooms)
    private String sortRooms;

    private String port;

    private boolean temporary = false;

    @XmllistAttribute("SORTROOMS")
    public void setSortRooms(String value) {
        this.sortRooms = value;
    }

    @XmllistAttribute("TEMPORARY")
    public void setTemporary(String value) {
        this.temporary = "1".equals(value);
    }

    @XmllistAttribute("PORT")
    public void setPort(String port) {
        this.port = port;
    }

    public String getHiddenRoom() {
        return hiddenRoom;
    }

    public String getHiddenGroup() {
        return hiddenGroup;
    }

    public String getSortRooms() {
        return sortRooms;
    }

    public List<String> getHiddenRooms() {
        if (hiddenRoom == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(hiddenRoom.split(","));
        }
    }

    @Override
    public boolean isSupported() {
        return !temporary && super.isSupported();
    }

    public List<String> getHiddenGroups() {
        if (hiddenGroup == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(hiddenGroup.split(","));
        }
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return FHEM;
    }

    public String getPort() {
        return port;
    }
}
