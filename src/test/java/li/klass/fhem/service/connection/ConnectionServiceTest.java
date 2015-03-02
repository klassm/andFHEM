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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.fhem.connection.ServerType;
import li.klass.fhem.testutil.MockitoTestRule;
import li.klass.fhem.util.ApplicationProperties;

import static li.klass.fhem.constants.PreferenceKeys.SELECTED_CONNECTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

@RunWith(DataProviderRunner.class)
public class ConnectionServiceTest {

    @Rule
    public MockitoTestRule mockitoTestRule = new MockitoTestRule();

    @InjectMocks
    private ConnectionService connectionService;

    @Mock
    private Context applicationContext;

    @Mock
    private ApplicationProperties applicationProperties;

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

        String json = ConnectionService.serialize(serverSpec);
        assertThat(json, is(not(nullValue())));

        FHEMServerSpec deserialized = ConnectionService.deserialize(json);

        assertThat(deserialized.getUrl(), is("http://test.com"));
        assertThat(deserialized.getUsername(), is("hallowelt"));
        assertThat(deserialized.getPassword(), is("myPassword"));
        assertThat(deserialized.getName(), is("MyServer"));
        assertThat(deserialized.getIp(), is("192.168.0.1"));
        assertThat(deserialized.getPort(), is(7072));
        assertThat(deserialized.getServerType(), is(ServerType.FHEMWEB));
    }

    @DataProvider
    public static Object[][] PORT_DATAPROVIDER() {
        return new Object[][]{
                {telnetSpecFor(8043), 8043},
                {fhemwebSpecFor("http://192.168.0.1:8084/fhem"), 8084},
                {fhemwebSpecFor("https://192.168.0.1:8084/fhem"), 8084},
                {fhemwebSpecFor("https://192.168.0.1:8084"), 8084},
                {fhemwebSpecFor("https://192.168.0.1/fhem"), 443},
                {fhemwebSpecFor("http://192.168.0.1/fhem"), 80},
                {dummySpec(), 0}
        };
    }

    @Test
    @UseDataProvider("PORT_DATAPROVIDER")
    public void should_extract_port(FHEMServerSpec spec, int expectedPort) {
        // given
        Context context = mock(Context.class);
        SharedPreferences sharedPreferences = mock(SharedPreferences.class);
        given(applicationProperties.getStringSharedPreference(eq(SELECTED_CONNECTION), anyString(), eq(context))).willReturn("a");
        given(context.getSharedPreferences(ConnectionService.PREFERENCES_NAME, Activity.MODE_PRIVATE)).willReturn(sharedPreferences);
        given(sharedPreferences.contains("a")).willReturn(true);
        given(sharedPreferences.getString("a", null)).willReturn(ConnectionService.serialize(spec));

        // when
        int port = connectionService.getPortOfSelectedConnection(context);

        // then
        assertThat(port).isEqualTo(expectedPort);
    }

    private static FHEMServerSpec telnetSpecFor(int port) {
        FHEMServerSpec spec = new FHEMServerSpec("a");
        spec.setServerType(ServerType.TELNET);
        spec.setPort(port);
        return spec;
    }

    private static FHEMServerSpec fhemwebSpecFor(String url) {
        FHEMServerSpec spec = new FHEMServerSpec("a");
        spec.setServerType(ServerType.FHEMWEB);
        spec.setUrl(url);
        return spec;
    }

    private static FHEMServerSpec dummySpec() {
        FHEMServerSpec spec = new FHEMServerSpec("a");
        spec.setServerType(ServerType.DUMMY);
        return spec;
    }
}
