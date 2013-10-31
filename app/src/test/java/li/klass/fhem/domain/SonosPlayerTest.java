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

import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class SonosPlayerTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        SonosPlayerDevice device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getCurrentAlbum(), isEmptyOrNullString());
        assertThat(device.getCurrentTitle(), isEmptyOrNullString());
        assertThat(device.getCurrentTitle(), isEmptyOrNullString());
        assertThat(device.getCurrentTrackDuration(), isEmptyOrNullString());
        assertThat(device.getInfoSummarize1(), Matchers.is("WDR 2 Rhein und Ruhr:"));
        assertThat(device.getInfoSummarize2(), Matchers.is("STOPPED => WDR 2 Rhein und Ruhr:"));
        assertThat(device.getInfoSummarize3(), Matchers.is("Lautstaerke: 24 ~ Ton An ~ Balance: Mitte ~ Kein Kopfhoerer"));
        assertThat(device.getMute(), Matchers.is("no"));
        assertThat(device.getNumberOfTracks(), Matchers.is("2"));
        assertThat(device.getRepeat(), Matchers.is("no"));
        assertThat(device.getShuffle(), Matchers.is("no"));
        assertThat(device.getVolume(), Matchers.is("24"));
        assertThat(device.getCurrentSender(), is("WDR 2 Rhein und Ruhr"));
    }

    @Override
    protected String getFileName() {
        return "sonosplayer.xml";
    }
}
