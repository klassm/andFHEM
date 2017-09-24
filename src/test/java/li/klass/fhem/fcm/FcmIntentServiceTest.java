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

package li.klass.fhem.fcm;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.util.Map;

import li.klass.fhem.testutil.MockitoRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

public class FcmIntentServiceTest {

    @InjectMocks
    FcmIntentService service;

    @Rule
    public MockitoRule mockitoRule = new MockitoRule();

    @Test
    public void should_read_device_state_updates_with_multiple_colons() {
        // when
        Map<String, String> changes = service.extractChanges("device", "temperature:18.9<|>T:18.9 H: 61");

        // then
        assertThat(changes).contains(entry("temperature", "18.9"), entry("state", "T:18.9 H: 61"));
    }
}