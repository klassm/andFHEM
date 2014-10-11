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

package li.klass.fhem.adapter.devices.genericui;

import android.content.Context;

import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import li.klass.fhem.domain.DummyDevice;
import li.klass.fhem.testutil.MockitoTestRule;

import static org.fest.assertions.api.Assertions.assertThat;

public class WebCmdActionRowTest {

    public MockitoTestRule mockitoTestRule = new MockitoTestRule();

    @Mock
    private Context context;

    @Test
    public void should_handle_null_webcmds() {
        // given
        DummyWebCmdRow row = new DummyWebCmdRow("row", 0);
        DummyDevice dummyDevice = new DummyDevice();

        // expect
        assertThat(dummyDevice.getWebCmd()).isEmpty();

        // when
        List<String> items = row.getItems(dummyDevice);

        // then
        assertThat(items).hasSize(0);
    }

    private class DummyWebCmdRow extends WebCmdActionRow<DummyDevice> {
        public DummyWebCmdRow(String description, int layout) {
            super(description, layout);
        }
    }
}