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

import static org.assertj.core.api.Assertions.assertThat;


public class CulHMDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testCanReadAllDevices() {
        assertThat(getDeviceFor("device", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("device1", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("Buero.Heiz_IN_Fenster", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("Buero.Heiz_IN_Klima", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("Buero.Heiz_IN_Wetter", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("Buero.Heiz_Steuerung", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("Buero.Heiz_Team", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("Buero.Heiz_Fernbedienung", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("Buero.Heizung", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("devicea", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("deviceb", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("deviceb", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("devicec", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("deviced", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("devicee", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("CUL_HM_HM_ES_PMSw1_Pl_24A7F1", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("devicef", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("deviceg", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("devicehh", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("devicei", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("device_relaxedEventMap", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("devicej", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("devicek", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("devicel", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("deviceWithPrefix", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("deviceWithMorePrefix", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("devicem", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("pressure", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("devicen", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("deviceo", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("oc3", GenericDevice.class)).isNotNull();
    }

    @Override
    protected String getFileName() {
        return "cul_hm.xml";
    }
}
