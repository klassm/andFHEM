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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import li.klass.fhem.domain.GenericDevice;
import li.klass.fhem.update.backend.xmllist.DeviceNode;
import li.klass.fhem.update.backend.xmllist.XmlListDevice;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class FhemDeviceTest {

    private static final Function<FhemDevice, String> TO_NAME = new Function<FhemDevice, String>() {
        @Override
        public String apply(FhemDevice input) {
            return input == null ? null : input.getName();
        }
    };

    @DataProvider
    public static Object[][] sortDatapoint() {
        return new Object[][]{
                {
                        asList(deviceFor("b", null, null), deviceFor("a", null, null), deviceFor("1", null, null)),
                        asList("1", "a", "b")
                },
                {
                        asList(deviceFor("b", null, null), deviceFor("a", "z", null), deviceFor("1", "x", null)),
                        asList("b", "1", "a")
                },
                {
                        asList(deviceFor("b", "c", "g"), deviceFor("a", "z", "a"), deviceFor("1", "x", "3")),
                        asList("1", "a", "b")
                }
        };
    }

    @Test
    @UseDataProvider("sortDatapoint")
    public void should_sort_devices(List<FhemDevice> devices, List<String> expectedDeviceNameOrder) {
        // given
        devices = newArrayList(devices);

        // when
        Collections.sort(devices, FhemDevice.BY_NAME);

        // then
        ImmutableList<String> names = from(devices).transform(TO_NAME).toList();
        assertThat(names).isEqualTo(expectedDeviceNameOrder);
    }

    private static FhemDevice deviceFor(String name, String alias, String sortBy) {
        GenericDevice device = new GenericDevice();
        XmlListDevice xmlListDevice = new XmlListDevice("dummy", new HashMap<String, DeviceNode>(), new HashMap<String, DeviceNode>(), new HashMap<String, DeviceNode>(), new HashMap<String, DeviceNode>());
        xmlListDevice.setInternal("NAME", name);
        xmlListDevice.setAttribute("alias", alias);
        device.setXmlListDevice(xmlListDevice);
        device.setSortBy(sortBy);

        return device;
    }
}