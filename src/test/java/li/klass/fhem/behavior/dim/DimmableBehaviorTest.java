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

package li.klass.fhem.behavior.dim;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import li.klass.fhem.domain.GenericDevice;
import li.klass.fhem.service.room.xmllist.DeviceNode;
import li.klass.fhem.service.room.xmllist.XmlListDevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(DataProviderRunner.class)
public class DimmableBehaviorTest {

    @Test
    public void should_create_discrete_behavior() {
        GenericDevice device = new GenericDevice();
        device.setSetList("dim10% dim20%");

        DimmableBehavior behavior = DimmableBehavior.behaviorFor(device).get();

        assertThat(behavior.getBehavior()).isInstanceOf(DiscreteDimmableBehavior.class);
        assertThat(behavior.getFhemDevice()).isSameAs(device);
    }

    @Test
    public void should_create_continuous_behavior() {
        GenericDevice device = new GenericDevice();
        device.setSetList("state:slider,0,1,100");

        DimmableBehavior behavior = DimmableBehavior.behaviorFor(device).get();

        assertThat(behavior.getBehavior()).isInstanceOf(ContinuousDimmableBehavior.class);
        assertThat(behavior.getFhemDevice()).isSameAs(device);
    }

    @Test
    public void should_return_absent_if_neither_continuous_nor_discrete_behavior_applies() {
        GenericDevice device = new GenericDevice();
        device.setSetList("on off");

        Optional<DimmableBehavior> result = DimmableBehavior.behaviorFor(device);

        assertThat(result).isEqualTo(Optional.absent());
    }

    @DataProvider
    public static Object[][] dimUpDownPositionProvider() {
        return new Object[][]{
                {50, 49, 51},
                {100, 99, 100},
                {0, 0, 1},
        };
    }

    @Test
    @UseDataProvider("dimUpDownPositionProvider")
    public void should_calculate_dim_up_and_down_positions(int currentPosition, int expectedDimDownPosition, int expectedDimUpPosition) {
        XmlListDevice xmlListDevice = mock(XmlListDevice.class);
        given(xmlListDevice.getStates()).willReturn(ImmutableMap.of("state", new DeviceNode(DeviceNode.DeviceNodeType.STATE, "state", currentPosition + "", null)));
        GenericDevice device = new GenericDevice();
        device.setXmlListDevice(xmlListDevice);
        device.setSetList("state:slider,0,1,100");

        DimmableBehavior behavior = DimmableBehavior.behaviorFor(device).get();

        assertThat(behavior.getDimUpPosition()).isEqualTo(expectedDimUpPosition);
        assertThat(behavior.getDimDownPosition()).isEqualTo(expectedDimDownPosition);
    }
}