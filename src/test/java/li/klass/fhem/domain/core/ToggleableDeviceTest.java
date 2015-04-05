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

package li.klass.fhem.domain.core;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import li.klass.fhem.domain.EIBDevice;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class ToggleableDeviceTest {

    @DataProvider
    public static Object[][] IS_ON_DATAPOINTS() {
        return new Object[][]{
                {"on", true},
                {"off", false},
                {"dim20%", true},
                {"off-for-timer 2000", false},
                {"on-for-timer 2000", true}
        };
    }

    @UseDataProvider("IS_ON_DATAPOINTS")
    @Test
    public void should_recognize_on_and_off_states_correctly(String readState, boolean isOn) {

        //  given
        ToggleableDevice device = new EIBDevice();
        device.setState(readState);

        // expect
        assertThat(device.isOffByState()).isEqualTo(!isOn);
        assertThat(device.isOnByState()).isEqualTo(isOn);
    }

    @UseDataProvider("IS_ON_DATAPOINTS")
    @Test
    public void should_handle_invert_state_hook(String readState, boolean isOn) {
        //  given
        ToggleableDevice device = new EIBDevice();
        device.setInvertState("true");

        ToggleableDevice device2 = new EIBDevice();
        device2.setInvertState("false");

        device.setState(readState);
        device2.setState(readState);

        // expect
        assertThat(device.isOnRespectingInvertHook()).isEqualTo(!isOn);
        assertThat(device2.isOnRespectingInvertHook()).isEqualTo(isOn);
    }
}