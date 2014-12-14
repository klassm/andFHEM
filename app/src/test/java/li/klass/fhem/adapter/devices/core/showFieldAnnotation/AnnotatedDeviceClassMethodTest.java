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

import java.lang.reflect.Method;

import li.klass.fhem.domain.FHTDevice;

import static li.klass.fhem.adapter.devices.core.showFieldAnnotation.AnnotatedDeviceClassMethod.getterNameToName;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AnnotatedDeviceClassMethodTest {
    @Test
    public void testGetterNameToName() throws Exception {
        assertThat(getterNameToName("getName"), is("name"));
        assertThat(getterNameToName("hello"), is("hello"));
    }

    @Test
    public void testGetDoubleValue() throws Exception {
        FHTDevice fhtDevice = new FHTDevice();
        fhtDevice.setDesiredTemp(20.0);

        String value = getValueFor(fhtDevice, "getDesiredTemp");
        assertThat(value, is("20.0"));
    }

    @Test
    public void testGetNullValue() throws Exception {
        FHTDevice fhtDevice = new FHTDevice();
        fhtDevice.setWarnings(null);

        String value = getValueFor(fhtDevice, "getWarnings");
        assertThat(value, is(nullValue()));
    }

    private String getValueFor(FHTDevice fhtDevice, String methodName) throws NoSuchMethodException {
        Method method = FHTDevice.class.getMethod(methodName);
        AnnotatedDeviceClassMethod pseudoAnnotatedMethod = new AnnotatedDeviceClassMethod(method);

        return pseudoAnnotatedMethod.getValueFor(fhtDevice);
    }
}
