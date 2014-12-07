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

package li.klass.fhem.service.room;

import com.google.common.base.Function;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import li.klass.fhem.domain.FHEMWEBDevice;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.testutil.MockitoTestRule;
import li.klass.fhem.util.ApplicationProperties;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.constants.PreferenceKeys.DEVICE_NAME;
import static li.klass.fhem.service.room.RoomListHolderService.DEFAULT_FHEMWEB_QUALIFIER;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(DataProviderRunner.class)
public class RoomListHolderServiceTest {

    public static final Function<String, Device> TO_FHEMWEB_DEVICE_WITH_NAME = new Function<String, Device>() {
        @Override
        public Device apply(String input) {
            FHEMWEBDevice device = new FHEMWEBDevice();
            device.readNAME(input);
            device.readGROUP("someGroup");
            return device;
        }
    };

    @Rule
    public MockitoTestRule mockitoTestRule = new MockitoTestRule();

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private RoomListHolderService service;

    @DataProvider
    public static Object[][] dataProvider_FHEMWEB_device_names() {
        return new Object[][]{
                {newArrayList("myDevice", "someDevice"), "myDevice", "myDevice"}, // find device with exact name
                {newArrayList("FHEMWEB_myDevice", "someDevice"), "myDevice", "FHEMWEB_myDevice"}, // finds device containing the qualifier
                {newArrayList("FHEMWEB_myDevice", "myDevice"), "myDevice", "FHEMWEB_myDevice"}, // takes the first device it finds (containing the qualifier)
                {newArrayList("FHEMWEB_myDevice", "someDevice"), "", "FHEMWEB_myDevice"}, // takes the first device if no device contains the qualifier
                {newArrayList("andFHEM_myDevice", "someDevice"), "", "andFHEM_myDevice"}, // defaults to andFHEM as qualifier
        };
    }

    @Test
    @UseDataProvider("dataProvider_FHEMWEB_device_names")
    public void should_find_the_correct_FHEMWEB_device(List<String> deviceNames, String qualifier, String expectedDeviceName) {
        // given
        List<Device> fhemwebDevices = from(deviceNames).transform(TO_FHEMWEB_DEVICE_WITH_NAME).toList();
        given(applicationProperties.getStringSharedPreference(DEVICE_NAME, DEFAULT_FHEMWEB_QUALIFIER)).willReturn(qualifier);

        // when
        FHEMWEBDevice foundDevice = service.findFHEMWEBDevice(fhemwebDevices);

        // then
        assertThat(foundDevice).isNotNull();
        assertThat(foundDevice.getName()).isEqualTo(expectedDeviceName);
    }
}