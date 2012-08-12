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

import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.infra.AndFHEMRobolectricTestRunner;
import li.klass.fhem.service.room.DeviceListParser;
import li.klass.fhem.util.CloseableUtil;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.util.Map;

@RunWith(AndFHEMRobolectricTestRunner.class)
public abstract class DeviceXMLParsingBase {

    public static final String DEFAULT_TEST_ROOM_NAME = "room";
    public static final String DEFAULT_TEST_DEVICE_NAME = "device";

    protected Map<String, RoomDeviceList> roomDeviceListMap;

    @Before
    public void loadDevices() throws Exception {
        InputStream inputStream = null;
        try {

            inputStream = getClass().getResourceAsStream(getFileName());

            String content = IOUtils.toString(inputStream);
            content = content.replaceAll("  ", "");

            roomDeviceListMap = DeviceListParser.INSTANCE.parseXMLList(content);
        } finally {
            CloseableUtil.close(inputStream);
        }
    }

    protected <T extends Device<T>> T getDefaultDevice() {
        return roomDeviceListMap.get(DEFAULT_TEST_ROOM_NAME).getDeviceFor(DEFAULT_TEST_DEVICE_NAME);
    }

    protected abstract String getFileName();
}
