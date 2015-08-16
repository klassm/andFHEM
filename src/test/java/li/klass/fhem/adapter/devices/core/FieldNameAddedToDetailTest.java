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

package li.klass.fhem.adapter.devices.core;

import android.content.Context;
import android.widget.TableLayout;
import android.widget.TableRow;

import org.junit.Before;
import org.junit.Test;

import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.domain.core.FhemDevice;

import static li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener.NotificationDeviceType.ALL;
import static li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener.NotificationDeviceType.DIMMER;
import static li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener.NotificationDeviceType.TOGGLEABLE_AND_NOT_DIMMABLE;
import static org.assertj.core.api.Assertions.assertThat;

public class FieldNameAddedToDetailTest {

    private FieldNameAddedToDetailListener listener;
    private CULHMDevice dimmableDevice;
    private CULHMDevice toggleableDevice;

    @Before
    public void before() {
        listener = new FieldNameAddedToDetailListener() {

            @Override
            protected void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, TableRow fieldTableRow) {
            }
        };

        dimmableDevice = new CULHMDevice();
        dimmableDevice.setSubType("DIMMER");

        toggleableDevice = new CULHMDevice();
        toggleableDevice.setSubType("SWITCH");
        toggleableDevice.setSetList("on off");
    }

    @Test
    public void testSupportsDeviceOfDimmerNotificationType() {
        listener.setNotificationDeviceType(DIMMER);

        assertThat(listener.supportsDevice(dimmableDevice)).isTrue();
        assertThat(listener.supportsDevice(toggleableDevice)).isFalse();
    }

    @Test
    public void testSupportsDeviceOfToggleableAndNotDimmableNotificationType() {
        listener.setNotificationDeviceType(TOGGLEABLE_AND_NOT_DIMMABLE);
        assertThat(listener.supportsDevice(dimmableDevice)).isFalse();
        assertThat(listener.supportsDevice(toggleableDevice)).isTrue();
    }

    @Test
    public void testSupportsDeviceOfAllNotificationType() {
        listener.setNotificationDeviceType(ALL);
        assertThat(listener.supportsDevice(dimmableDevice)).isTrue();
        assertThat(listener.supportsDevice(toggleableDevice)).isTrue();
    }
}
