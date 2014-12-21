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

package li.klass.fhem.service.device;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import li.klass.fhem.domain.GCMSendDevice;
import li.klass.fhem.testutil.MockitoTestRule;
import li.klass.fhem.util.ApplicationProperties;

import static li.klass.fhem.constants.BundleExtraKeys.GCM_REGISTRATION_ID;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

public class GCMSendDeviceServiceTest {
    @Rule
    public MockitoTestRule mockitoTestRule = new MockitoTestRule();

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private GCMSendDeviceService service;

    @Test
    public void testIsDeviceRegistered() {
        GCMSendDevice device = new GCMSendDevice();

        assertThat(service.isDeviceRegistered(device)).isFalse();

        device.readREGIDS("abc|def");
        assertThat(service.isDeviceRegistered(device)).isFalse();

        given(applicationProperties.getStringSharedPreference(eq(GCM_REGISTRATION_ID), anyString())).willReturn("abc");
        assertThat(service.isDeviceRegistered(device)).isTrue();

        given(applicationProperties.getStringSharedPreference(eq(GCM_REGISTRATION_ID), anyString())).willReturn("def");
        assertThat(service.isDeviceRegistered(device)).isTrue();
    }
}