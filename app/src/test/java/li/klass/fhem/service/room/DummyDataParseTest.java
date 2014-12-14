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

package li.klass.fhem.service.room;

import android.content.Context;

import com.google.common.io.CharStreams;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.InputStream;
import java.io.InputStreamReader;

import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.fhem.DummyDataConnection;
import li.klass.fhem.infra.AndFHEMRobolectricTestRunner;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.testutil.MockitoTestRule;
import li.klass.fhem.util.CloseableUtil;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(AndFHEMRobolectricTestRunner.class)
public class DummyDataParseTest {
    @Rule
    public MockitoTestRule mockitoTestRule = new MockitoTestRule();
    @InjectMocks
    protected DeviceListParser deviceListParser = new DeviceListParser();
    @Mock
    ConnectionService connectionService;
    @Mock
    Context applicationContext;

    @Test
    public void testParseDummyData() throws Exception {
        InputStream dummyDataStream = DummyDataConnection.class.getResourceAsStream("dummyData.xml");
        assertThat(dummyDataStream, is(not(nullValue())));

        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(dummyDataStream);
            String xmlList = CharStreams.toString(reader);
            assertNotNull(xmlList);

            RoomDeviceList result = deviceListParser.parseAndWrapExceptions(xmlList);

            assertNotNull(result);
        } finally {
            CloseableUtil.close(dummyDataStream, reader);
        }
    }
}
