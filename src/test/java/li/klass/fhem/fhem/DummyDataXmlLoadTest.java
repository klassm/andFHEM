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

package li.klass.fhem.fhem;

import android.annotation.TargetApi;
import android.os.Build;

import org.assertj.core.util.Files;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import li.klass.fhem.domain.core.FhemDevice;

import static org.assertj.core.api.Assertions.assertThat;

public class DummyDataXmlLoadTest extends DeviceXMLParsingBase {
    @Test
    public void testFunctionalityIsSetOnAllDevices() {
        for (FhemDevice device : roomDeviceList.getAllDevices()) {
            assertThat(device.getDeviceGroup()).as(device.getName()).isNotNull();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testCanSerialize() throws IOException {
        File file = Files.newTemporaryFile();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file))) {
            objectOutputStream.writeObject(roomDeviceList);
        }
    }

    @Override
    protected String getFileName() {
        return "dummyData.xml";
    }

    @Override
    protected Class<?> getTestFileBaseClass() {
        return DummyDataConnection.class;
    }
}
