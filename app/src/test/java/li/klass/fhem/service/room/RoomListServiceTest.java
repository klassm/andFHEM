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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.infra.basetest.RobolectricBaseTestCase;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.testutil.MockitoTestRule;
import li.klass.fhem.util.ApplicationProperties;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static li.klass.fhem.constants.PreferenceKeys.DEVICE_NAME;
import static li.klass.fhem.domain.core.RoomDeviceList.ALL_DEVICES_ROOM;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;

public class RoomListServiceTest extends RobolectricBaseTestCase {

    @Rule
    public MockitoTestRule mockitoTestRule = new MockitoTestRule();

    @Mock
    private ConnectionService connectionService;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private RoomListHolderService roomListHolderService;

    @InjectMocks
    private RoomListService service;

    @Before
    public void before() {
        given(applicationProperties.getStringSharedPreference(DEVICE_NAME, "andFHEM")).willReturn("abc");
        given(connectionService.mayShowInCurrentConnectionType(any(DeviceType.class))).willCallRealMethod();
        doCallRealMethod().when(roomListHolderService).findFHEMWEBDevice(any(RoomDeviceList.class));
    }

    @Test
    public void get_room_names_with_supported_devices() throws Exception {
        service.deviceList = new RoomDeviceList(ALL_DEVICES_ROOM);
        service.deviceList.addDevice(new TestDevice("a", true, "abc", "def"));
        service.deviceList.addDevice(new TestDevice("b", true, "def", "fgh"));

        assertThat(service.getRoomNameList()).containsExactly("abc", "def", "fgh");
    }

    @Test
    public void get_room_names_with_unsupported_devices() throws Exception {
        service.deviceList = new RoomDeviceList(ALL_DEVICES_ROOM);
        service.deviceList.addDevice(new TestDevice("a", true, "abc", "def"));
        service.deviceList.addDevice(new TestDevice("b", false, "def", "fgh"));

        assertThat(service.getRoomNameList()).containsExactly("abc", "def");
    }

    @DataProvider
    public static Object[][] dataProviderSortRooms() {
        return new Object[][]{
                {"A B C", newHashSet("A", "B", "C"), newArrayList("A", "B", "C")}
        };
    }

    class TestDevice extends FS20Device {

        private final boolean supported;

        TestDevice(String name, boolean supported, String... rooms) {
            setName(name);
            setRooms(newArrayList(rooms));
            readGROUP("TEST");
            this.supported = supported;
        }

        @Override
        public boolean isSupported() {
            return supported;
        }
    }
}
