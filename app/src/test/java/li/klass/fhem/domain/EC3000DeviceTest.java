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

public class EC3000DeviceTest extends DeviceXMLParsingBase {

    @Test
    public void should_read_device_correctly() {
        EC3000Device device = getDefaultDevice();

        assertThat(device.getState()).isEqualTo("68 (W)");
        assertThat(device.getConsumption()).isEqualTo("13.782 (kWh)");
        assertThat(device.getPower()).isEqualTo("68 (W)");
        assertThat(device.getMeasured()).isEqualTo("2014-05-04 14:14:37");
        assertThat(device.getPrice()).isEqualTo("3.29 (€)");
        assertThat(device.getWidgetInfoLine()).isEqualTo("3.29 (€), 13.782 (kWh)");
    }

    @Override
    protected String getFileName() {
        return "ec3000.xml";
    }
}
