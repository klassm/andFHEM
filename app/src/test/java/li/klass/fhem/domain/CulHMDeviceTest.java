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
        assertThat(getDeviceFor("device")).isNotNull();
        assertThat(getDeviceFor("device1")).isNotNull();
        assertThat(getDeviceFor("Buero.Heiz_IN_Fenster")).isNotNull();
        assertThat(getDeviceFor("Buero.Heiz_IN_Klima")).isNotNull();
        assertThat(getDeviceFor("Buero.Heiz_IN_Wetter")).isNotNull();
        assertThat(getDeviceFor("Buero.Heiz_Steuerung")).isNotNull();
        assertThat(getDeviceFor("Buero.Heiz_Team")).isNotNull();
        assertThat(getDeviceFor("Buero.Heiz_Fernbedienung")).isNotNull();
        assertThat(getDeviceFor("Buero.Heizung")).isNotNull();
        assertThat(getDeviceFor("devicea")).isNotNull();
        assertThat(getDeviceFor("deviceb")).isNotNull();
        assertThat(getDeviceFor("deviceb")).isNotNull();
        assertThat(getDeviceFor("devicec")).isNotNull();
        assertThat(getDeviceFor("deviced")).isNotNull();
        assertThat(getDeviceFor("devicee")).isNotNull();
        assertThat(getDeviceFor("CUL_HM_HM_ES_PMSw1_Pl_24A7F1")).isNotNull();
        assertThat(getDeviceFor("devicef")).isNotNull();
        assertThat(getDeviceFor("deviceg")).isNotNull();
        assertThat(getDeviceFor("devicehh")).isNotNull();
        assertThat(getDeviceFor("devicei")).isNotNull();
        assertThat(getDeviceFor("device_relaxedEventMap")).isNotNull();
        assertThat(getDeviceFor("devicej")).isNotNull();
        assertThat(getDeviceFor("devicek")).isNotNull();
        assertThat(getDeviceFor("devicel")).isNotNull();
        assertThat(getDeviceFor("deviceWithPrefix")).isNotNull();
        assertThat(getDeviceFor("deviceWithMorePrefix")).isNotNull();
        assertThat(getDeviceFor("devicem")).isNotNull();
        assertThat(getDeviceFor("pressure")).isNotNull();
        assertThat(getDeviceFor("devicen")).isNotNull();
        assertThat(getDeviceFor("deviceo")).isNotNull();
        assertThat(getDeviceFor("oc3")).isNotNull();
    }

    @Override
    protected String getFileName() {
        return "cul_hm.xml";
    }
}
