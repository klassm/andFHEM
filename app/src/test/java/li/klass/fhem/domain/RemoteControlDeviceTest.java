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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class RemoteControlDeviceTest extends DeviceXMLParsingBase {

    @Test
    public void testAttributesDefaultDevice() {
        RemoteControlDevice device = getDefaultDevice();

        assertThat(device, is(not(nullValue())));

        assertThat(device.getIconPath(), is("icons/remotecontrol"));
        assertThat(device.getIconPrefix(), is("black_btn_"));

        assertThat(device.getChannel(), is("kabel eins"));
        assertThat(device.getCurrentTitle(), is("Cold Case - Kein Opfer ist je vergessen"));

        List<List<RemoteControlDevice.Entry>> rows = device.getRows();
        assertThat(rows.size(), is(14));

        List<RemoteControlDevice.Entry> row0 = rows.get(0);
        assertThat(row0.size(), is(3));
        assertThat(row0.get(0).command, is("POWEROFF"));
        assertThat(row0.get(0).icon, is("POWEROFF"));
        assertThat(row0.get(1).command, is("TV"));
        assertThat(row0.get(2).command, is("HDMI"));
        assertThat(row0.get(2).getIconPath(), is("/icons/remotecontrol/black_btn_HDMI.png"));

        List<RemoteControlDevice.Entry> row1 = rows.get(1);
        assertThat(row1.size(), is(3));
        assertThat(row1.get(0).command, is(""));
        assertThat(row1.get(0).icon, is("blank"));
        assertThat(row1.get(1).command, is(""));
        assertThat(row1.get(2).command, is(""));

        List<RemoteControlDevice.Entry> row2 = rows.get(2);
        assertThat(row2.size(), is(3));
        assertThat(row2.get(0).command, is("1"));
        assertThat(row2.get(1).command, is("2"));
        assertThat(row2.get(2).command, is("3"));

        List<RemoteControlDevice.Entry> row3 = rows.get(3);
        assertThat(row3.size(), is(3));
        assertThat(row3.get(0).command, is("4"));
        assertThat(row3.get(1).command, is("5"));
        assertThat(row3.get(2).command, is("6"));

        List<RemoteControlDevice.Entry> row4 = rows.get(4);
        assertThat(row4.size(), is(3));
        assertThat(row4.get(0).command, is("7"));
        assertThat(row4.get(1).command, is("8"));
        assertThat(row4.get(2).command, is("9"));

        List<RemoteControlDevice.Entry> row7 = rows.get(7);
        assertThat(row7.size(), is(3));
        assertThat(row7.get(0).command, is("VOLUP"));
        assertThat(row7.get(1).command, is("MUTE"));
        assertThat(row7.get(2).command, is("channelUP"));
        assertThat(row7.get(2).icon, is("CHUP"));
    }

    @Override
    protected String getFileName() {
        return "remotecontrol.xml";
    }
}
