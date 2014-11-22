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

import org.junit.Test;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.fest.assertions.api.Assertions.assertThat;

public class EnigmaDeviceTest extends DeviceXMLParsingBase {

    @Test
    public void should_read_device_attributes() {
        EnigmaDevice device = getDeviceFor("Wz.TvReceiver");
        assertThat(device).isNotNull();
        assertThat(device.getChannel()).isEqualTo("a");
        assertThat(device.getCurrentTitle()).isEqualTo("b");
        assertThat(device.getPower()).isEqualTo("off");
        assertThat(device.getServiceName()).isEqualTo("s");
        assertThat(device.getEventDurationHour()).isEqualTo("edh");
        assertThat(device.getEventDurationNextHour()).isEqualTo("ednh");
        assertThat(device.getEventNameNext()).isEqualTo("enn");
        assertThat(device.getEventRemainingHour()).isEqualTo("30");
        assertThat(device.getEventStart()).isEqualTo("20:15");
        assertThat(device.getEventStartNextHour()).isEqualTo("21:15");
        assertThat(device.getHdd1Capacity()).isEqualTo("750.156");
        assertThat(device.getHdd1Free()).isEqualTo("695.119");
        assertThat(device.getInput()).isEqualTo("audio");
        assertThat(device.getLanmac()).isEqualTo("53:15:ed:16:c0:75");
        assertThat(device.getModel()).isEqualTo("duo2");
        assertThat(device.getMute()).isEqualTo("-");
        assertThat(device.getVideoSize()).isEqualTo("1024x768");
        assertThat(device.getVolume()).isEqualTo("40");
    }

    @Override
    protected String getFileName() {
        return "enigma.xml";
    }
}