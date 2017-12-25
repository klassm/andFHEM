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
import li.klass.fhem.domain.core.FhemDevice;

import static org.assertj.core.api.Assertions.assertThat;

public class SonosPlayerTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        FhemDevice device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(stateValueFor(device, "currentAlbum")).isNullOrEmpty();
        assertThat(stateValueFor(device, "currentTitle")).isNullOrEmpty();
        assertThat(stateValueFor(device, "currentTrackDuration")).isEqualTo("0:00:00");
        assertThat(stateValueFor(device, "infoSummarize1")).isEqualTo("WDR 2 Rhein und Ruhr:");
        assertThat(stateValueFor(device, "infoSummarize2")).isEqualTo("STOPPED => WDR 2 Rhein und Ruhr:");
        assertThat(stateValueFor(device, "infoSummarize3")).isEqualTo("Lautstaerke: 24 ~ Ton An ~ Balance: Mitte ~ Kein Kopfhoerer");
        assertThat(stateValueFor(device, "Mute")).isEqualTo("0");
        assertThat(stateValueFor(device, "numberOfTracks")).isEqualTo("2");
        assertThat(stateValueFor(device, "Repeat")).isEqualTo("0");
        assertThat(stateValueFor(device, "Shuffle")).isEqualTo("0");
        assertThat(stateValueFor(device, "Volume")).isEqualTo("24");
        assertThat(stateValueFor(device, "currentSender")).isEqualTo("WDR 2 Rhein und Ruhr");
    }

    @Override
    protected String getFileName() {
        return "sonosplayer.xml";
    }
}
