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

import org.assertj.core.data.Offset;
import org.junit.Test;

import li.klass.fhem.behavior.dim.DimmableBehavior;
import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import li.klass.fhem.domain.core.FhemDevice;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadingsProxyDeviceTest extends DeviceXMLParsingBase {

    @Test
    public void testRGBProxy() {
        FhemDevice device = getDeviceFor("rgb");

        assertThat(device).isNotNull();
        assertThat(device.getName()).isEqualTo("rgb");
        assertThat(device.getXmlListDevice().getState("rgb", true)).isEqualTo("FFFF00");
    }

    @Test
    public void testSwitchProxy() {
        FhemDevice device = getDeviceFor("Bild");

        assertThat(device).isNotNull();
        assertThat(device.getXmlListDevice().getState("rgb", true)).isNull();
    }

    @Test
    public void testDimmable() {
        FhemDevice device = getDeviceFor("dimmable");

        assertThat(device).isNotNull();
        DimmableBehavior dimmableBehavior = DimmableBehavior.Companion.behaviorFor(device, null).get();
        assertThat(dimmableBehavior.getCurrentDimPosition()).isEqualTo(100, Offset.offset(0.01));
        assertThat(dimmableBehavior.getDimLowerBound()).isEqualTo(4);
        assertThat(dimmableBehavior.getDimUpperBound()).isEqualTo(105);
        assertThat(dimmableBehavior.getDimStep()).isEqualTo(1);
    }

    @Override
    protected String getFileName() {
        return "readings_proxy.xml";
    }
}
