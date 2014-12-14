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

package li.klass.fhem.adapter.devices.core;

import org.junit.Test;

import java.util.List;

import li.klass.fhem.adapter.devices.core.showFieldAnnotation.AnnotatedDeviceClassItem;
import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.domain.HCSDevice;

import static li.klass.fhem.adapter.devices.core.DeviceFields.getSortedAnnotatedClassItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.fail;

public class DeviceFieldsTest {

    @Test
    public void testHCSOrder() {
        List<AnnotatedDeviceClassItem> items = getSortedAnnotatedClassItems(HCSDevice.class);

        int numberOfDemandDevicesIndex = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getName().equalsIgnoreCase("numberOfDemandDevices")) {
                numberOfDemandDevicesIndex = i;
                break;
            }
        }

        if (numberOfDemandDevicesIndex == -1) {
            fail("cannot find demandDevices attribute index.");
        }

        assertThat(items.get(numberOfDemandDevicesIndex + 1).getName(), is("commaSeparatedDemandDevices"));
    }

    @Test
    public void testFHTOrder() {
        List<AnnotatedDeviceClassItem> items = getSortedAnnotatedClassItems(FHTDevice.class);

        int temperatureIndex = -1;
        int desiredTempIndex = -1;
        int windowOpenTempIndex = -1;
        int dayTempIndex = -1;
        int nightTempIndex = -1;

        for (int i = 0; i < items.size(); i++) {
            AnnotatedDeviceClassItem item = items.get(i);

            String name = item.getName();
            if (name.equalsIgnoreCase("temperature")) {
                temperatureIndex = i;
            } else if (name.equalsIgnoreCase("desiredTemp")) {
                desiredTempIndex = i;
            } else if (name.equalsIgnoreCase("dayTemperature")) {
                dayTempIndex = i;
            } else if (name.equalsIgnoreCase("nightTemperature")) {
                nightTempIndex = i;
            } else if (name.equalsIgnoreCase("windowOpenTemp")) {
                windowOpenTempIndex = i;
            }
        }

        assertThat(temperatureIndex, is(not(-1)));
        assertThat(desiredTempIndex, is(not(-1)));
        assertThat(windowOpenTempIndex, is(not(-1)));
        assertThat(dayTempIndex, is(not(-1)));
        assertThat(nightTempIndex, is(not(-1)));

        assertThat(temperatureIndex, is(lessThan(desiredTempIndex)));
        assertThat(desiredTempIndex, is(lessThan(dayTempIndex)));
        assertThat(dayTempIndex, is(lessThan(nightTempIndex)));
        assertThat(nightTempIndex, is(lessThan(windowOpenTempIndex)));
    }
}
