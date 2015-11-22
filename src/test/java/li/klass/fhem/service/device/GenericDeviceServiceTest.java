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

package li.klass.fhem.service.device;

import android.content.Context;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import li.klass.fhem.domain.GenericDevice;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.deviceConfiguration.DeviceConfiguration;
import li.klass.fhem.service.room.xmllist.XmlListDevice;
import li.klass.fhem.testutil.MockitoRule;

import static org.mockito.Mockito.verify;

@RunWith(DataProviderRunner.class)
public class GenericDeviceServiceTest {
    @Rule
    public MockitoRule mockitoRule = new MockitoRule();

    @InjectMocks
    GenericDeviceService genericDeviceService;

    @Mock
    CommandExecutionService commandExecutionService;

    @Mock
    Context context;

    @Test
    public void should_not_set_substate_state() {
        // given
        GenericDevice device = new GenericDevice();
        XmlListDevice xmllistDevice = new XmlListDevice("FS20");
        xmllistDevice.setInternal("NAME", "someName");
        device.setXmlListDevice(xmllistDevice);

        // when
        genericDeviceService.setSubState(device, "state", "bla", context);

        // then
        verify(commandExecutionService).executeSafely("set someName bla", context);
    }

    @DataProvider
    public static Object[][] stateReplaceProvider() {
        return new Object[][]{
                {"4.5", "on"},
                {"4.5,10.3", "on,10.3"},
                {"30.5", "off"},
                {"30.5,5.5", "off,5.5"},
                {"14.5", "14.5"},
        };
    }

    @Test
    @UseDataProvider("stateReplaceProvider")
    public void should_replace_state_value(String toSet, String expected) {
        // given
        toSet = "weekProfile " + toSet;
        expected = "weekProfile " + expected;
        GenericDevice device = new GenericDevice();
        String stateName = "myState";
        device.setDeviceConfiguration(Optional.of(new DeviceConfiguration.Builder().withCommandReplace(stateName, ImmutableMap.of("4\\.5", "on", "30\\.5", "off")).build()));
        XmlListDevice xmllistDevice = new XmlListDevice("FS20");
        xmllistDevice.setInternal("NAME", "someName");
        device.setXmlListDevice(xmllistDevice);

        // when
        genericDeviceService.setSubState(device, stateName, toSet, context);

        // then
        verify(commandExecutionService).executeSafely("set someName " + stateName + " " + expected, context);
    }
}