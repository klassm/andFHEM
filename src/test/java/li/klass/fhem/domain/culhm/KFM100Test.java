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

package li.klass.fhem.domain.culhm;

import org.junit.Test;

import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public class KFM100Test extends DeviceXMLParsingBase {

    @Test
    public void testForCorrectlySetAttributes() {
        CULHMDevice device = getDefaultDevice(CULHMDevice.class);

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("171.3 (l)");
        assertThat(device.getSubType()).isEqualTo(CULHMDevice.SubType.FILL_STATE);
        assertThat(device.supportsDim()).isFalse();

        assertThat(device.getRawValue()).isEqualTo("20");
        assertThat(device.getFillContentLitresMaximum()).isEqualTo(4198);
        assertThat(device.getFillContentPercentage()).isEqualTo("4 (%)");
        assertThat(device.getFillContentPercentageRaw()).isCloseTo(0.04, offset(0.01));
        assertThat(device.getFillContentLitresRaw()).isCloseTo(171.3, offset(0.01));
        assertThat(device.getRawToReadable()).isEqualTo("10:0 255:4198");

        device.setContent("4300l");
        device.afterAllXMLRead();
        assertThat(device.getFillContentPercentageRaw()).isCloseTo(1, offset(0.01));

        assertThat(device.isSupported()).isTrue();
    }

    @Override
    protected String getFileName() {
        return "kfm100.xml";
    }
}
