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

import static li.klass.fhem.domain.core.DeviceFunctionality.HEATING;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ChannelsTest extends DeviceXMLParsingBase {
    @Test
    public void testChannelsAreFoundAndRecognisedAsVisible() {

        CULHMDevice heizung = getDeviceFor("Buero.Heizung");
        assertThat(heizung, is(notNullValue()));
        assertThat(heizung.isSupported(), is(true));
        assertThat(heizung.getSubType(), is(CULHMDevice.SubType.HEATING));
        assertThat(heizung.getDeviceGroup(), is(HEATING));

        CULHMDevice wetter = getDeviceFor("Buero.Heiz_IN_Wetter");
        assertThat(wetter, is(notNullValue()));
        assertThat(wetter.isSupported(), is(true));
        assertThat(wetter.getSubType(), is(CULHMDevice.SubType.HEATING));
        assertThat(wetter.getDeviceGroup(), is(HEATING));

        CULHMDevice klima = getDeviceFor("Buero.Heiz_IN_Klima");
        assertThat(klima, is(notNullValue()));
        assertThat(klima.isSupported(), is(true));
        assertThat(klima.getSubType(), is(CULHMDevice.SubType.HEATING));
        assertThat(klima.getDeviceGroup(), is(HEATING));

        CULHMDevice fenster = getDeviceFor("Buero.Heiz_IN_Fenster");
        assertThat(fenster, is(notNullValue()));
        assertThat(fenster.isSupported(), is(true));
        assertThat(fenster.getSubType(), is(CULHMDevice.SubType.HEATING));
        assertThat(fenster.getDeviceGroup(), is(HEATING));

        CULHMDevice steuerung = getDeviceFor("Buero.Heiz_Steuerung");
        assertThat(steuerung, is(notNullValue()));
        assertThat(steuerung.isSupported(), is(true));
        assertThat(steuerung.getSubType(), is(CULHMDevice.SubType.HEATING));
        assertThat(steuerung.getDeviceGroup(), is(HEATING));

        CULHMDevice team = getDeviceFor("Buero.Heiz_Team");
        assertThat(team, is(notNullValue()));
        assertThat(team.isSupported(), is(true));
        assertThat(team.getSubType(), is(CULHMDevice.SubType.HEATING));
        assertThat(team.getDeviceGroup(), is(HEATING));

        CULHMDevice fernbedienung = getDeviceFor("Buero.Heiz_Fernbedienung");
        assertThat(fernbedienung, is(notNullValue()));
        assertThat(fernbedienung.isSupported(), is(true));
        assertThat(fernbedienung.getSubType(), is(CULHMDevice.SubType.HEATING));
        assertThat(fernbedienung.getDeviceGroup(), is(HEATING));
    }

    @Override
    protected String getFileName() {
        return "channels.xml";
    }
}
