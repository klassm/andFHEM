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

import android.content.Context;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import li.klass.fhem.domain.FHEMWEBDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.testutil.MockitoTestRule;
import li.klass.fhem.util.ApplicationProperties;

import static com.google.common.collect.FluentIterable.from;
import static li.klass.fhem.constants.PreferenceKeys.FHEMWEB_DEVICE_NAME;
import static li.klass.fhem.service.room.RoomListHolderService.DEFAULT_FHEMWEB_QUALIFIER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Strings.isNullOrEmpty;
import static org.mockito.BDDMockito.given;

@RunWith(DataProviderRunner.class)
public class RoomListHolderServiceTest {

    public static final Function<Pair<String, Integer>, FhemDevice> TO_FHEMWEB_DEVICE_WITH_NAME = new Function<Pair<String, Integer>, FhemDevice>() {
        @Override
        public FhemDevice apply(Pair<String, Integer> input) {
            FHEMWEBDevice device = new FHEMWEBDevice();
            device.setName(input.getLeft());
            device.setPort(String.valueOf(input.getRight()));
            device.setGroup("someGroup");
            return device;
        }
    };

    @Rule
    public MockitoTestRule mockitoTestRule = new MockitoTestRule();

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private Context context;

    @Mock
    private ConnectionService connectionService;

    @InjectMocks
    private RoomListHolderService service;

    @DataProvider
    public static Object[][] FHEMWEB_DEVICE_NAMES() {
        return new Object[][]{
                { // find device with exact name
                        ImmutableList.of(Pair.of("myDevice", 80), Pair.of("someDevice", 80)),
                        80,
                        "myDevice",
                        "myDevice"
                },
                { // finds device containing the qualifier
                        ImmutableList.of(Pair.of("FHEMWEB_myDevice", 80), Pair.of("someDevice", 80)),
                        80,
                        "myDevice",
                        "FHEMWEB_myDevice"},
                { // takes the first device it finds (containing the qualifier)
                        ImmutableList.of(Pair.of("FHEMWEB_myDevice", 80), Pair.of("myDevice", 80)),
                        80,
                        "myDevice",
                        "FHEMWEB_myDevice"
                },
                { // takes the first device if no device contains the qualifier
                        ImmutableList.of(Pair.of("FHEMWEB_myDevice", 80), Pair.of("someDevice", 80)),
                        80,
                        "",
                        "FHEMWEB_myDevice"
                },
                { // defaults to andFHEM as qualifier if port is not found
                        ImmutableList.of(Pair.of("andFHEM_myDevice", 80), Pair.of("someDevice", 80)),
                        80,
                        "",
                        "andFHEM_myDevice"
                },
                { // defaults to port if qualifier is unset
                        ImmutableList.of(Pair.of("andFHEM_myDevice", 80), Pair.of("someDevice", 8084)),
                        8084,
                        "",
                        "someDevice"
                },
                { // defaults to andFHEM as qualifier, as no device matches the selected device's port
                        ImmutableList.of(Pair.of("andFHEM_myDevice", 80), Pair.of("someDevice", 8084)),
                        8083,
                        "",
                        "andFHEM_myDevice"
                },
        };
    }

    @Test
    @UseDataProvider("FHEMWEB_DEVICE_NAMES")
    public void should_find_the_correct_FHEMWEB_device(List<Pair<String, Integer>> deviceNames, int selectedPort,
                                                       String qualifier, String expectedDeviceName) {
        // given
        List<FhemDevice> fhemwebDevices = from(deviceNames).transform(TO_FHEMWEB_DEVICE_WITH_NAME).toList();
        given(applicationProperties.containsSharedPreference(context, FHEMWEB_DEVICE_NAME)).willReturn(!isNullOrEmpty(qualifier));
        given(applicationProperties.getStringSharedPreference(FHEMWEB_DEVICE_NAME, DEFAULT_FHEMWEB_QUALIFIER, context)).willReturn(qualifier);
        given(connectionService.getPortOfSelectedConnection(context)).willReturn(selectedPort);

        // when
        FHEMWEBDevice foundDevice = service.findFHEMWEBDevice(fhemwebDevices, context);

        // then
        assertThat(foundDevice).isNotNull();
        assertThat(foundDevice.getName()).isEqualTo(expectedDeviceName);
    }
}