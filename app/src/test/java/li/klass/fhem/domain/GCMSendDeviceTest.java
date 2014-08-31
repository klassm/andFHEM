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

package li.klass.fhem.domain;

import org.junit.Test;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GCMSendDeviceTest extends DeviceXMLParsingBase {

    @Test
    public void testAttributesProperlySet() {
        GCMSendDevice device = getDeviceFor("gcm");
        assertThat(device.getApiKey(), is("AIzaSyCs7OxUcPp5"));
        assertThat(device.getRegIds(), hasItemInArray("APA91bHTAy8Xp4uE4FyCJuMnAn"));
        assertThat(device.getRegIds(), hasItemInArray("BAPA91bHTGy8Xp5uE4FyCJuMnAn"));
    }

    @Override
    protected String getFileName() {
        return "gcmsend.xml";
    }
}
