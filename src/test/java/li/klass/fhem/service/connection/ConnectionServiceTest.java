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

package li.klass.fhem.service.connection;

import android.content.Context;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.fhem.connection.ServerType;
import li.klass.fhem.infra.basetest.RobolectricBaseTestCase;
import li.klass.fhem.testutil.MockitoTestRule;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class ConnectionServiceTest extends RobolectricBaseTestCase {

    @Rule
    public MockitoTestRule mockitoTestRule = new MockitoTestRule();

    @InjectMocks
    private ConnectionService connectionService;

    @Mock
    private Context applicationContext;

    @Test
    public void testFHEMServerSpecSerializeDeserialize() {
        FHEMServerSpec serverSpec = new FHEMServerSpec("test");
        serverSpec.setUrl("http://test.com");
        serverSpec.setUsername("hallowelt");
        serverSpec.setPassword("myPassword");
        serverSpec.setName("MyServer");
        serverSpec.setIp("192.168.0.1");
        serverSpec.setPort(7072);
        serverSpec.setServerType(ServerType.FHEMWEB);

        String json = connectionService.serialize(serverSpec);
        assertThat(json, is(not(nullValue())));

        FHEMServerSpec deserialized = connectionService.deserialize(json);

        assertThat(deserialized.getUrl(), is("http://test.com"));
        assertThat(deserialized.getUsername(), is("hallowelt"));
        assertThat(deserialized.getPassword(), is("myPassword"));
        assertThat(deserialized.getName(), is("MyServer"));
        assertThat(deserialized.getIp(), is("192.168.0.1"));
        assertThat(deserialized.getPort(), is(7072));
        assertThat(deserialized.getServerType(), is(ServerType.FHEMWEB));
    }
}
