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

package li.klass.fhem.adapter.devices.core.showFieldAnnotation;

import org.junit.Test;

import java.lang.reflect.Field;

import li.klass.fhem.domain.FHTDevice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AnnotatedDeviceClassFieldTest {
    @Test
    public void testGetDoubleValue() throws Exception {
        FHTDevice fhtDevice = new FHTDevice();
        fhtDevice.setDesiredTemp(20.0);

        String value = getFieldValue(fhtDevice, "desiredTemp");
        assertThat(value, is("20.0"));
    }

    @Test
    public void testGetNullValue() throws Exception {
        FHTDevice fhtDevice = new FHTDevice();
        fhtDevice.setHeatingMode(null);

        String value = getFieldValue(fhtDevice, "heatingMode");
        assertThat(value, is(nullValue()));
    }

    private String getFieldValue(FHTDevice fhtDevice, String fieldName) throws NoSuchFieldException {
        Field field = FHTDevice.class.getDeclaredField(fieldName);
        AnnotatedDeviceClassField pseudoAnnotatedField = new AnnotatedDeviceClassField(field);

        return pseudoAnnotatedField.getValueFor(fhtDevice);
    }
}
