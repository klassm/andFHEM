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

import java.util.List;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.assertj.core.api.Assertions.assertThat;

public class RemoteControlDeviceTest extends DeviceXMLParsingBase {

    @Test
    public void testAttributesDefaultDevice() {
        RemoteControlDevice device = getDefaultDevice(RemoteControlDevice.class);

        assertThat(device).isNotNull();

        assertThat(device.getIconPath()).isEqualTo("icons/remotecontrol");
        assertThat(device.getIconPrefix()).isEqualTo("black_btn_");

        assertThat(device.getChannel()).isEqualTo("kabel eins");
        assertThat(device.getCurrentTitle()).isEqualTo("Cold Case - Kein Opfer ist je vergessen");

        List<List<RemoteControlDevice.Entry>> rows = device.getRows();
        assertThat(rows.size()).isEqualTo(14);

        List<RemoteControlDevice.Entry> row0 = rows.get(0);
        assertThat(row0.size()).isEqualTo(3);
        assertThat(row0.get(0).command).isEqualTo("POWEROFF");
        assertThat(row0.get(0).icon).isEqualTo("POWEROFF");
        assertThat(row0.get(1).command).isEqualTo("TV");
        assertThat(row0.get(2).command).isEqualTo("HDMI");
        assertThat(row0.get(2).getIconPath()).isEqualTo("/icons/remotecontrol/black_btn_HDMI.png");

        List<RemoteControlDevice.Entry> row1 = rows.get(1);
        assertThat(row1.size()).isEqualTo(3);
        assertThat(row1.get(0).command).isEqualTo("");
        assertThat(row1.get(0).icon).isEqualTo("blank");
        assertThat(row1.get(1).command).isEqualTo("");
        assertThat(row1.get(2).command).isEqualTo("");

        List<RemoteControlDevice.Entry> row2 = rows.get(2);
        assertThat(row2.size()).isEqualTo(3);
        assertThat(row2.get(0).command).isEqualTo("1");
        assertThat(row2.get(1).command).isEqualTo("2");
        assertThat(row2.get(2).command).isEqualTo("3");

        List<RemoteControlDevice.Entry> row3 = rows.get(3);
        assertThat(row3.size()).isEqualTo(3);
        assertThat(row3.get(0).command).isEqualTo("4");
        assertThat(row3.get(1).command).isEqualTo("5");
        assertThat(row3.get(2).command).isEqualTo("6");

        List<RemoteControlDevice.Entry> row4 = rows.get(4);
        assertThat(row4.size()).isEqualTo(3);
        assertThat(row4.get(0).command).isEqualTo("7");
        assertThat(row4.get(1).command).isEqualTo("8");
        assertThat(row4.get(2).command).isEqualTo("9");

        List<RemoteControlDevice.Entry> row7 = rows.get(7);
        assertThat(row7.size()).isEqualTo(3);
        assertThat(row7.get(0).command).isEqualTo("VOLUP");
        assertThat(row7.get(1).command).isEqualTo("MUTE");
        assertThat(row7.get(2).command).isEqualTo("channelUP");
        assertThat(row7.get(2).icon).isEqualTo("CHUP");
    }

    @Override
    protected String getFileName() {
        return "remotecontrol.xml";
    }
}
