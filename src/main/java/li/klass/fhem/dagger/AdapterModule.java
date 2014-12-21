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

package li.klass.fhem.dagger;

import dagger.Module;
import li.klass.fhem.adapter.devices.CULHMAdapter;
import li.klass.fhem.adapter.devices.DmxAdapter;
import li.klass.fhem.adapter.devices.DummyAdapter;
import li.klass.fhem.adapter.devices.EnOceanAdapter;
import li.klass.fhem.adapter.devices.EnigmaDeviceAdapter;
import li.klass.fhem.adapter.devices.FHTAdapter;
import li.klass.fhem.adapter.devices.FS20ZDRDeviceAdapter;
import li.klass.fhem.adapter.devices.FloorplanAdapter;
import li.klass.fhem.adapter.devices.GCMSendDeviceAdapter;
import li.klass.fhem.adapter.devices.HueDeviceAdapter;
import li.klass.fhem.adapter.devices.LightSceneAdapter;
import li.klass.fhem.adapter.devices.MaxAdapter;
import li.klass.fhem.adapter.devices.OnkyoAvrDeviceAdapter;
import li.klass.fhem.adapter.devices.OwSwitchDeviceAdapter;
import li.klass.fhem.adapter.devices.PCA9532DeviceAdapter;
import li.klass.fhem.adapter.devices.PCF8574DeviceAdapter;
import li.klass.fhem.adapter.devices.PidAdapter;
import li.klass.fhem.adapter.devices.PioneerAvrDeviceAdapter;
import li.klass.fhem.adapter.devices.ReadingsProxyDeviceAdapter;
import li.klass.fhem.adapter.devices.RemoteControlAdapter;
import li.klass.fhem.adapter.devices.SonosPlayerAdapter;
import li.klass.fhem.adapter.devices.SwapDeviceAdapter;
import li.klass.fhem.adapter.devices.SwitchActionRowAdapter;
import li.klass.fhem.adapter.devices.ThresholdAdapter;
import li.klass.fhem.adapter.devices.ToggleableAdapterWithSwitchActionRow;
import li.klass.fhem.adapter.devices.UniRollAdapter;
import li.klass.fhem.adapter.devices.WOLAdapter;
import li.klass.fhem.adapter.devices.WeatherAdapter;
import li.klass.fhem.adapter.devices.WebLinkAdapter;
import li.klass.fhem.adapter.devices.WifiLightDeviceAdapter;
import li.klass.fhem.adapter.devices.YamahaAVRAdapter;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.adapter.devices.core.DimmableAdapter;
import li.klass.fhem.adapter.devices.core.GenericDeviceAdapter;
import li.klass.fhem.adapter.devices.core.ToggleableAdapter;

@Module(complete = false,
        injects = {
                CULHMAdapter.class,
                DmxAdapter.class,
                DummyAdapter.class,
                EnOceanAdapter.class,
                FHTAdapter.class,
                FloorplanAdapter.class,
                FS20ZDRDeviceAdapter.class,
                GCMSendDeviceAdapter.class,
                HueDeviceAdapter.class,
                LightSceneAdapter.class,
                MaxAdapter.class,
                OwSwitchDeviceAdapter.class,
                PCA9532DeviceAdapter.class,
                PCF8574DeviceAdapter.class,
                PidAdapter.class,
                ReadingsProxyDeviceAdapter.class,
                RemoteControlAdapter.class,
                SonosPlayerAdapter.class,
                SwapDeviceAdapter.class,
                SwitchActionRowAdapter.class,
                ThresholdAdapter.class,
                ToggleableAdapter.class,
                UniRollAdapter.class,
                WeatherAdapter.class,
                WebLinkAdapter.class,
                WifiLightDeviceAdapter.class,
                WOLAdapter.class,
                YamahaAVRAdapter.class,
                OnkyoAvrDeviceAdapter.class,
                EnigmaDeviceAdapter.class,
                PioneerAvrDeviceAdapter.class,

                DeviceAdapter.class,
                GenericDeviceAdapter.class,
                DimmableAdapter.class,
                ToggleableAdapterWithSwitchActionRow.class,
                SwitchActionRowAdapter.class

        })
public class AdapterModule {
}
