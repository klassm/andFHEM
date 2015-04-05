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

package li.klass.fhem.domain.core;

import android.content.Context;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.service.room.DeviceListParser;
import li.klass.fhem.service.room.xmllist.XmlListParser;
import li.klass.fhem.testsuite.category.DeviceTestBase;
import li.klass.fhem.testutil.MockitoRule;
import li.klass.fhem.util.CloseableUtil;
import li.klass.fhem.util.ReflectionUtil;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

@Category(DeviceTestBase.class)
public abstract class DeviceXMLParsingBase {

    public static final String DEFAULT_TEST_ROOM_NAME = "room";
    public static final String DEFAULT_TEST_DEVICE_NAME = "device";

    protected RoomDeviceList roomDeviceList;

    @Mock
    protected ConnectionService connectionService;

    @InjectMocks
    protected DeviceListParser deviceListParser;

    @Rule
    public MockitoRule mockitoRule = new MockitoRule();

    @Mock
    protected Context context;

    @Before
    public void before() throws Exception {
        AndFHEMApplication.setContext(context);

        XmlListParser xmlListParser = AndFHEMApplication.createDaggerGraph().get(XmlListParser.class);
        ReflectionUtil.setFieldValue(deviceListParser, "parser", xmlListParser);
        mockStrings();

        doReturn(true).when(connectionService).mayShowInCurrentConnectionType(any(DeviceType.class), eq(context));

        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        try {

            inputStream = getTestFileBaseClass().getResourceAsStream(getFileName());

            if (inputStream == null) {
                throw new IllegalArgumentException("cannot find " + getFileName());
            }

            inputStreamReader = new InputStreamReader(inputStream, Charsets.UTF_8);
            String content = CharStreams.toString(inputStreamReader);

            roomDeviceList = deviceListParser.parseXMLListUnsafe(content, context);
        } finally {
            CloseableUtil.close(inputStream, inputStreamReader);
        }
    }

    protected void mockStrings() {
        try {
            String content = Resources.toString(new File("src/main/res/values/strings.xml").getAbsoluteFile().toURI().toURL(), Charsets.UTF_8);
            Pattern pattern = Pattern.compile("<string name=\"([^\"]+)\">([^<]+)</string>");
            Matcher matcher = pattern.matcher(content);

            Map<String, String> values = Maps.newHashMap();
            while (matcher.find()) {
                values.put(matcher.group(1), matcher.group(2));
            }

            for (Field field : R.string.class.getDeclaredFields()) {
                int value = (int) field.get(R.string.class);
                given(context.getString(value)).willReturn(values.get(field.getName()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Base class used as context for loading the input file.
     *
     * @return base class
     */
    protected Class<?> getTestFileBaseClass() {
        return getClass();
    }

    protected abstract String getFileName();

    protected <T extends FhemDevice<T>> T getDefaultDevice(Class<T> clazz) {
        return getDeviceFor(DEFAULT_TEST_DEVICE_NAME, clazz);
    }

    // Careful: The Java-Compiler needs some class instance of <T> here to infer the type correctly!
    protected <T extends FhemDevice<T>> T getDeviceFor(String deviceName, @SuppressWarnings("unused") Class<T> clazz) {
        return roomDeviceList.getDeviceFor(deviceName);
    }
}
