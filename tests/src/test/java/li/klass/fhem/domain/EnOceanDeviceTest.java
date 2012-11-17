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

import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class EnOceanDeviceTest extends DeviceXMLParsingBase {

    @Test
    public void testForCorrectlySetAttributes() {
        EnOceanDevice device = getDefaultDevice();
        assertThat(device.getSubType(), is(EnOceanDevice.SubType.SWITCH));
        assertThat(device.getState(), is("on"));
        assertThat(device.isOn(), is(true));
        assertThat(device.getEventMapStateFor("BI"), is("off"));
        assertThat(device.getEventMapStateFor("B0"), is("on"));

        device.setState("B0");
        assertThat(device.getState(), is("on"));

        EnOceanDevice device1 = getDeviceFor("device1");
        assertThat(device1.getSubType(), is(EnOceanDevice.SubType.SENSOR));
        assertThat(device1.getState(), is("153"));
        assertThat(device1.getMeasured(), is("2012-11-04 23:55:11"));

        device.readSUBTYPE("");
        assertThat(device.compareTo(device1), is(-1));
    }

    @Override
    protected String getFileName() {
        return "enocean.xml";
    }
}
