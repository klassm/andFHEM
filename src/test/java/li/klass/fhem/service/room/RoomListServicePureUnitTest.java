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

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import li.klass.fhem.domain.FHEMWEBDevice;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.testutil.MockitoTestRule;
import li.klass.fhem.util.ApplicationProperties;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static li.klass.fhem.constants.PreferenceKeys.DEVICE_NAME;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

// We cannot combine two runners, so we have to create an extra test class for only
// parametrized unit tests ...
@RunWith(DataProviderRunner.class)
public class RoomListServicePureUnitTest {

    @Rule
    public MockitoTestRule mockitoTestRule = new MockitoTestRule();

    @Mock
    private ConnectionService connectionService;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private RoomListService service;

    @Before
    public void before() {
        given(applicationProperties.getStringSharedPreference(DEVICE_NAME, "andFHEM")).willReturn("abc");
        given(connectionService.mayShowInCurrentConnectionType(any(DeviceType.class))).willCallRealMethod();
    }

    @DataProvider
    public static Object[][] dataProviderSortRooms() {
        return new Object[][]{
                {"A B C", newHashSet("A", "B", "C"), newArrayList("A", "B", "C")},
                {"Z K", newHashSet("A", "Z", "K"), newArrayList("Z", "K", "A")},
                {"", newHashSet("Z", "B", "X", "K"), newArrayList("B", "K", "X", "Z")},
                {"Z", newHashSet("B", "Z", "X", "K"), newArrayList("Z", "B", "K", "X")}
        };
    }

    @Test
    @UseDataProvider("dataProviderSortRooms")
    public void should_sort_rooms(String sortRoomsAttribute, Set<String> roomNames, List<String> expectedRooms) {
        // given
        FHEMWEBDevice device = new FHEMWEBDevice();
        device.readSORTROOMS(sortRoomsAttribute);

        // when
        ArrayList<String> result = service.sortRooms(roomNames, device);

        // then
        assertThat(result).isEqualTo(expectedRooms);
    }

}
