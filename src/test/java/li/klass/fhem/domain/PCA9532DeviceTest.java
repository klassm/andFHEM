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

import java.util.Map;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.data.MapEntry.entry;

public class PCA9532DeviceTest extends DeviceXMLParsingBase {

    @Test
    public void all_properties_read() {
        PCA9532Device device = getDeviceFor("PCA9532");

        Map<String, Boolean> portsMap = device.getPortsIsOnMap();
        assertThat(portsMap).contains(
                entry("Port0", true),
                entry("Port1", false),
                entry("Port2", true),
                entry("Port3", true),
                entry("Port4", false),
                entry("Port5", false)
        );

        assertThat(device.getPwm0()).isEqualTo(128);
        assertThat(device.getPwm1()).isEqualTo(129);
    }

    @Override
    protected String getFileName() {
        return "pca9532.xml";
    }
}
