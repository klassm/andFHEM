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

import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.service.room.xmllist.DeviceNode;
import li.klass.fhem.service.room.xmllist.XmlListDevice;
import li.klass.fhem.testutil.MockitoRule;
import li.klass.fhem.util.ApplicationProperties;

import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.constants.PreferenceKeys.FHEMWEB_DEVICE_NAME;
import static li.klass.fhem.domain.core.RoomDeviceList.ALL_DEVICES_ROOM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;

public class RoomListServiceTest {

    @Rule
    public MockitoRule mockitoRule = new MockitoRule();

    @Mock
    private ConnectionService connectionService;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private RoomListHolderService roomListHolderService;

    @Mock
    private Context context;

    @InjectMocks
    private RoomListService service;

    private RoomDeviceList roomDeviceList;

    @Before
    public void before() {
        roomDeviceList = new RoomDeviceList(ALL_DEVICES_ROOM);
        given(applicationProperties.getStringSharedPreference(eq(FHEMWEB_DEVICE_NAME), eq("andFHEM"), eq(context))).willReturn("abc");
        given(connectionService.mayShowInCurrentConnectionType(any(DeviceType.class), eq(context))).willCallRealMethod();
        given(roomListHolderService.getCachedRoomDeviceListMap()).willReturn(roomDeviceList);
        doCallRealMethod().when(roomListHolderService).findFHEMWEBDevice(any(RoomDeviceList.class), eq(context));
    }

    @Test
    public void get_room_names_with_supported_devices() throws Exception {
        roomDeviceList.addDevice(new TestDevice("a", true, "abc", "def"), context);
        roomDeviceList.addDevice(new TestDevice("b", true, "def", "fgh"), context);

        assertThat(service.getRoomNameList(context)).containsExactly("abc", "def", "fgh");
    }

    @Test
    public void get_room_names_with_unsupported_devices() throws Exception {
        roomDeviceList.addDevice(new TestDevice("a", true, "abc", "def"), context);
        roomDeviceList.addDevice(new TestDevice("b", false, "def", "fgh"), context);

        assertThat(service.getRoomNameList(context)).containsExactly("abc", "def");
    }

    class TestDevice extends FS20Device {

        private final boolean supported;

        TestDevice(String name, boolean supported, String... rooms) {
            setName(name);
            setXmlListDevice(new XmlListDevice(DeviceType.GENERIC.getXmllistTag(), Maps.<String,
                    DeviceNode>newHashMap(), Maps.<String, DeviceNode>newHashMap(), Maps.<String, DeviceNode>newHashMap(), Maps.<String, DeviceNode>newHashMap()));
            setRooms(newArrayList(rooms));
            this.supported = supported;
        }

        @Override
        public boolean isSupported() {
            return supported;
        }
    }
}
