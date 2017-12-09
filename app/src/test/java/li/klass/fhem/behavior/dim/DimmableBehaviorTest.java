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

import android.support.annotation.NonNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import li.klass.fhem.domain.GenericDevice;
import li.klass.fhem.update.backend.xmllist.DeviceNode;
import li.klass.fhem.update.backend.xmllist.XmlListDevice;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class DimmableBehaviorTest {

    @Test
    public void should_create_discrete_behavior() {
        GenericDevice device = deviceFor("dim10% dim20%");

        DimmableBehavior behavior = DimmableBehavior.behaviorFor(device, null).get();

        assertThat(behavior.getBehavior()).isInstanceOf(DiscreteDimmableBehavior.class);
        assertThat(behavior.getFhemDevice()).isSameAs(device);
    }

    @Test
    public void should_create_continuous_behavior() {
        GenericDevice device = deviceFor("state:slider,0,1,100");

        DimmableBehavior behavior = DimmableBehavior.behaviorFor(device, null).get();

        assertThat(behavior.getBehavior()).isInstanceOf(ContinuousDimmableBehavior.class);
        assertThat(behavior.getFhemDevice()).isSameAs(device);
    }

    @Test
    public void should_return_absent_if_neither_continuous_nor_discrete_behavior_applies() {
        GenericDevice device = deviceFor("on off");

        Optional<DimmableBehavior> result = DimmableBehavior.behaviorFor(device, null);

        assertThat(result).isEqualTo(Optional.absent());
    }

    @NonNull
    private GenericDevice deviceFor(String setList) {
        GenericDevice device = new GenericDevice();
        device.setXmlListDevice(new XmlListDevice(
                "generic", new HashMap<String, DeviceNode>(), new HashMap<String, DeviceNode>(), new HashMap<String, DeviceNode>(),
                ImmutableMap.of("sets", new DeviceNode(DeviceNode.DeviceNodeType.HEADER, "sets", setList, ""))
        ));
        return device;
    }
}