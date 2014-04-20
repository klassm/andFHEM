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

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.ShowField;

import static li.klass.fhem.domain.core.DeviceFunctionality.FHEM;

@SuppressWarnings("unused")
public class FHEMWEBDevice extends Device<FHEMWEBDevice> {

    @ShowField(description = ResourceIdMapper.hiddenRooms)
    private String hiddenRoom;

    @ShowField(description = ResourceIdMapper.hiddenGroups)
    private String hiddenGroup;

    @ShowField(description = ResourceIdMapper.sortRooms)
    private String sortRooms;

    private boolean temporary = false;

    public void readHIDDENROOM(String value) {
        this.hiddenRoom = value;
    }

    public void readHIDDENGROUP(String value) {
        this.hiddenGroup = value;
    }

    public void readSORTROOMS(String value) {
        this.sortRooms = value;
    }

    public void readTEMPORARY(String value) {
        this.temporary = value.equals("1");
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

    @Override
    public boolean isSupported() {
        return !temporary;
    }
}
