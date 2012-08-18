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
import li.klass.fhem.domain.FS20Device;
import org.junit.Before;
import org.junit.Test;

import static li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener.NotificationDeviceType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FieldNameAddedToDetailTest {

    private FieldNameAddedToDetailListener<FS20Device> listener;
    private FS20Device dimmableFS20Device;
    private FS20Device toggleableFS20Device;

    @Before
    public void before() {
        listener = new FieldNameAddedToDetailListener<FS20Device>() {

            @Override
            protected void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FS20Device device, TableRow fieldTableRow) {
            }
        };

        dimmableFS20Device = new FS20Device();
        dimmableFS20Device.readMODEL(FS20Device.dimModels.get(0));

        toggleableFS20Device = new FS20Device();
        toggleableFS20Device.readMODEL("NOT_DIMMABLE");
    }

    @Test
    public void testSupportsDeviceOfDimmerNotificationType() {
        listener.setNotificationDeviceType(DIMMER);

        assertThat(listener.supportsDevice(dimmableFS20Device), is(true));
        assertThat(listener.supportsDevice(toggleableFS20Device), is(false));
    }

    @Test
    public void testSupportsDeviceOfToggleableAndNotDimmableNotificationType() {
        listener.setNotificationDeviceType(TOGGLEABLE_AND_NOT_DIMMABLE);
        assertThat(listener.supportsDevice(dimmableFS20Device), is(false));
        assertThat(listener.supportsDevice(toggleableFS20Device), is(true));
    }

    @Test
    public void testSupportsDeviceOfAllNotificationType() {
        listener.setNotificationDeviceType(ALL);
        assertThat(listener.supportsDevice(dimmableFS20Device), is(true));
        assertThat(listener.supportsDevice(toggleableFS20Device), is(true));
    }
}
