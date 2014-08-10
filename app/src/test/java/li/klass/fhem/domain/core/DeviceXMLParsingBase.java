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

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.experimental.categories.Category;

import java.io.InputStream;

import li.klass.fhem.infra.basetest.RobolectricBaseTestCase;
import li.klass.fhem.service.room.DeviceListParser;
import li.klass.fhem.testsuite.category.DeviceTestBase;
import li.klass.fhem.util.CloseableUtil;

@Category(DeviceTestBase.class)
public abstract class DeviceXMLParsingBase extends RobolectricBaseTestCase {

    public static final String DEFAULT_TEST_ROOM_NAME = "room";
    public static final String DEFAULT_TEST_DEVICE_NAME = "device";

    protected RoomDeviceList roomDeviceList;

    @Before
    public void loadDevices() throws Exception {
        InputStream inputStream = null;
        try {

            inputStream = getTestFileBaseClass().getResourceAsStream(getFileName());

            if (inputStream == null) {
                throw new IllegalArgumentException("cannot find " + getFileName());
            }
            String content = IOUtils.toString(inputStream);

            roomDeviceList = DeviceListParser.INSTANCE.parseAndWrapExceptions(content);
        } finally {
            CloseableUtil.close(inputStream);
        }
    }

    protected <T extends Device<T>> T getDefaultDevice() {
        return getDeviceFor(DEFAULT_TEST_DEVICE_NAME);
    }

    protected <T extends Device<T>> T getDeviceFor(String deviceName) {
        return roomDeviceList.getDeviceFor(deviceName);
    }

    protected abstract String getFileName();

    /**
     * Base class used as context for loading the input file.
     *
     * @return base class
     */
    protected Class<?> getTestFileBaseClass() {
        return getClass();
    }
}
