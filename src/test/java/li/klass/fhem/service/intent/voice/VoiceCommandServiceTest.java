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

package li.klass.fhem.service.intent.voice;

import android.content.Context;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import com.tngtech.java.junit.dataprovider.*;
import li.klass.fhem.domain.*;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.service.room.xmllist.XmlListDevice;
import li.klass.fhem.testutil.MockitoRule;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;

import java.util.List;

import static com.tngtech.java.junit.dataprovider.DataProviders.*;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(DataProviderRunner.class)
public class VoiceCommandServiceTest {

    @Rule
    public MockitoRule mockitoRule = new MockitoRule();

    @InjectMocks
    private VoiceCommandService service;

    @Mock
    private RoomListService roomListService;
    @Mock
    private Context context;

    @DataProvider
    public static Object[][] voiceCommandsForDevice() {
        return $$(
                $(new CommandTestCase().withCommand("schalte lampe ein").withExpectedResult(new VoiceResult.Success("lampe", "on")).withDeviceName("lampe")),
                $(new CommandTestCase().withCommand("schalke lampe ein").withExpectedResult(new VoiceResult.Success("lampe", "on")).withDeviceName("lampe")),
                $(new CommandTestCase().withCommand("schalke lampe 1").withExpectedResult(new VoiceResult.Success("lampe", "on")).withDeviceName("lampe")),
                $(new CommandTestCase().withCommand("schalke lampe an").withExpectedResult(new VoiceResult.Success("lampe", "on")).withDeviceName("lampe")),
                $(new CommandTestCase().withCommand("schalte lampe aus").withExpectedResult(new VoiceResult.Success("lampe", "off")).withDeviceName("lampe")),
                $(new CommandTestCase().withCommand("switch lampe on").withExpectedResult(new VoiceResult.Success("lampe", "on")).withDeviceName("lampe")),
                $(new CommandTestCase().withCommand("switch lampe off").withExpectedResult(new VoiceResult.Success("lampe", "off")).withDeviceName("lampe")),
                $(new CommandTestCase().withCommand("schalte den lampe ein").withExpectedResult(new VoiceResult.Success("lampe", "on")).withDeviceName("lampe")),
                $(new CommandTestCase().withCommand("schalte der lampe ein").withExpectedResult(new VoiceResult.Success("lampe", "on")).withDeviceName("lampe")),
                $(new CommandTestCase().withCommand("schalte die lampe ein").withExpectedResult(new VoiceResult.Success("lampe", "on")).withDeviceName("lampe")),
                $(new CommandTestCase().withCommand("schalte das lampe ein").withExpectedResult(new VoiceResult.Success("lampe", "on")).withDeviceName("lampe")),
                $(new CommandTestCase().withCommand("schalte the lampe ein").withExpectedResult(new VoiceResult.Success("lampe", "on")).withDeviceName("lampe")),
                $(new CommandTestCase().withCommand("schalte fritz wlan ein").withExpectedResult(new VoiceResult.Success("fritz wlan", "on")).withDeviceName("fritz wlan"))
        );
    }

    @Test
    @UseDataProvider("voiceCommandsForDevice")
    public void should_find_out_correct_voice_result(CommandTestCase testCase) {
        // given
        TestDummy device = new TestDummy(testCase.deviceName);
        device.getSetList().parse("on off");
        RoomDeviceList deviceList = new RoomDeviceList("").addDevice(device, context);
        doReturn(deviceList).when(roomListService).getAllRoomsDeviceList(Optional.absent(), context);

        // when
        Optional<VoiceResult> result = service.resultFor(testCase.command, context);

        // then
        assertThat(result).contains(testCase.voiceResult);
    }

    @Test
    public void should_handle_event_maps() {
        // given
        TestDummy device = new TestDummy("lampe");
        device.getSetList().parse("on off");
        device.setEventmap("on:hallo");
        RoomDeviceList deviceList = new RoomDeviceList("").addDevice(device, context);
        doReturn(deviceList).when(roomListService).getAllRoomsDeviceList(Optional.absent(), context);

        // when
        Optional<VoiceResult> result = service.resultFor("schalte lampe hallo", context);

        // then
        assertThat(result).contains(new VoiceResult.Success("lampe", "on"));
    }

    @Test
    public void should_ignore_devices_not_containing_the_target_state_in_the_setlist() {
        // given
        TestDummy device = new TestDummy("lampe");
        device.getSetList().parse("off");
        RoomDeviceList deviceList = new RoomDeviceList("").addDevice(device, context);
        doReturn(deviceList).when(roomListService).getAllRoomsDeviceList(Optional.absent(), context);

        // when
        Optional<VoiceResult> result = service.resultFor("schalte lampe on", context);

        // then
        assertThat(result).contains(new VoiceResult.Error(VoiceResult.ErrorType.NO_DEVICE_MATCHED));
    }

    @Test
    public void should_return_error_if_no_device_matches_a_command() {
        // given
        TestDummy device = new TestDummy("lampe1");
        device.getSetList().parse("on off");
        RoomDeviceList deviceList = new RoomDeviceList("").addDevice(device, context);
        doReturn(deviceList).when(roomListService).getAllRoomsDeviceList(Optional.absent(), context);

        // when
        Optional<VoiceResult> result = service.resultFor("schalte lampe 1", context);

        // then
        assertThat(result).contains(new VoiceResult.Error(VoiceResult.ErrorType.NO_DEVICE_MATCHED));
    }

    @Test
    public void should_return_error_if_more_than_one_device_matches_a_command() {
        // given
        TestDummy device = new TestDummy("lampe1", "lampe");
        TestDummy device1 = new TestDummy("lampe2", "lampe");
        device.getSetList().parse("on off");
        device1.getSetList().parse("on off");
        RoomDeviceList deviceList = new RoomDeviceList("").addDevice(device, context).addDevice(device1, context);
        doReturn(deviceList).when(roomListService).getAllRoomsDeviceList(Optional.absent(), context);

        // when
        Optional<VoiceResult> result = service.resultFor("schalte lampe 1", context);

        // then
        assertThat(result).contains(new VoiceResult.Error(VoiceResult.ErrorType.MORE_THAN_ONE_DEVICE_MATCHES));
    }

    @DataProvider
    public static Object[][] shortcutCommandsForDevice() {
        return new Object[][]{
                {new CommandTestCase().withCommand("starte lampe").withExpectedResult(new VoiceResult.Success("lampe", "on")).withDeviceName("lampe")},
                {new CommandTestCase().withCommand("stoppe lampe").withExpectedResult(new VoiceResult.Success("lampe", "off")).withDeviceName("lampe")},
                {new CommandTestCase().withCommand("beende lampe").withExpectedResult(new VoiceResult.Success("lampe", "off")).withDeviceName("lampe")},
                {new CommandTestCase().withCommand("beginne lampe").withExpectedResult(new VoiceResult.Success("lampe", "on")).withDeviceName("lampe")},
                {new CommandTestCase().withCommand("start lampe").withExpectedResult(new VoiceResult.Success("lampe", "on")).withDeviceName("lampe")},
                {new CommandTestCase().withCommand("stop lampe").withExpectedResult(new VoiceResult.Success("lampe", "off")).withDeviceName("lampe")},
                {new CommandTestCase().withCommand("end lampe").withExpectedResult(new VoiceResult.Success("lampe", "off")).withDeviceName("lampe")},
                {new CommandTestCase().withCommand("begin lampe").withExpectedResult(new VoiceResult.Success("lampe", "on")).withDeviceName("lampe")},
                {new CommandTestCase().withCommand("starte garten leuchte").withExpectedResult(new VoiceResult.Success("lampe", "on")).withDeviceName("lampe")}
        };
    }

    @Test
    @UseDataProvider("shortcutCommandsForDevice")
    public void should_handle_shortcuts(CommandTestCase commandTestCase) {
        // given
        TestDummy device = new TestDummy(commandTestCase.deviceName);
        device.setPronunciation("garten leuchte");
        device.getSetList().parse("on off");
        RoomDeviceList deviceList = new RoomDeviceList("").addDevice(device, context);
        doReturn(deviceList).when(roomListService).getAllRoomsDeviceList(Optional.absent(), context);

        // when
        Optional<VoiceResult> result = service.resultFor(commandTestCase.command, context);

        // then
        assertThat(result).contains(commandTestCase.voiceResult);
    }


    @DataProvider
    public static Object[][] pronunciation() {
        return $$(
                $("voice"),
                $("voice blub")
        );
    }

    @UseDataProvider("pronunciation")
    @Test
    public void should_treat_voice_pronunciation_attribute(String pronunciation) {
        // given
        TestDummy device = new TestDummy("lampe");
        device.getSetList().parse("on off");
        device.setPronunciation(pronunciation);
        RoomDeviceList deviceList = new RoomDeviceList("").addDevice(device, context);
        doReturn(deviceList).when(roomListService).getAllRoomsDeviceList(Optional.absent(), context);

        // when
        Optional<VoiceResult> result = service.resultFor("set " + pronunciation + " on", context);

        // then
        assertThat(result).contains(new VoiceResult.Success("lampe", "on"));
    }

    @Test
    public void should_handle_light_scenes() {
        // given
        LightSceneDevice lightSceneDevice = new LightSceneDevice() {
            @Override
            public List<String> getInternalDeviceGroupOrGroupAttributes(Context context) {
                return Lists.newArrayList("group");
            }
        };
        lightSceneDevice.setXmlListDevice(new XmlListDevice("dummy"));
        lightSceneDevice.getXmlListDevice().setInternal("NAME", "device");
        lightSceneDevice.getSetList().parse("scene:off,on");
        RoomDeviceList deviceList = new RoomDeviceList("").addDevice(lightSceneDevice, context);
        doReturn(deviceList).when(roomListService).getAllRoomsDeviceList(Optional.absent(), context);

        // when
        Optional<VoiceResult> result = service.resultFor("set device on", context);

        // then
        assertThat(result).contains(new VoiceResult.Success("device", "scene on"));

        // when
        result = service.resultFor("set device off", context);

        // then
        assertThat(result).contains(new VoiceResult.Success("device", "scene off"));
    }

    public class TestDummy extends GenericDevice {

        TestDummy(String name) {
            XmlListDevice xmlListDevice = new XmlListDevice("dummy");
            xmlListDevice.setInternal("NAME", name);
            setXmlListDevice(xmlListDevice);
        }

        TestDummy(String name, String alias) {
            this(name);
            getXmlListDevice().setAttribute("alias", alias);
        }

        @Override
        public List<String> getInternalDeviceGroupOrGroupAttributes(Context context) {
            return ImmutableList.of("group");
        }

    }

    private static class CommandTestCase {
        String command;
        private VoiceResult voiceResult;
        private String deviceName;

        CommandTestCase withCommand(String command) {
            this.command = command;
            return this;
        }

        CommandTestCase withExpectedResult(VoiceResult voiceResult) {
            this.voiceResult = voiceResult;
            return this;
        }

        CommandTestCase withDeviceName(String deviceName) {
            this.deviceName = deviceName;
            return this;
        }
    }
}