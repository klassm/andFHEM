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

import android.graphics.Bitmap;
import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import li.klass.fhem.fhem.connection.DummyServerSpec;

public class DummyDataConnection extends FHEMConnection {
    public static final DummyDataConnection INSTANCE = new DummyDataConnection();
    private static final Logger LOG = LoggerFactory.getLogger(DummyDataConnection.class);
    public static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");

    private DummyDataConnection() {
    }

    @Override
    public RequestResult<String> executeCommand(String command) {
        LOG.error("executeCommand() - execute command {}", command);

        if (command.equalsIgnoreCase("xmllist")) return xmllist();
        if (command.startsWith("get")) return fileLogData(command);

        return new RequestResult<>("I am a dummy. Do you expect me to answer you?");
    }

    private RequestResult<String> xmllist() {
        try {
            LOG.info("xmllist() - loading xmllist");
            final DummyServerSpec dummyServerSpec = (DummyServerSpec) serverSpec;
            URL url = Resources.getResource(DummyDataConnection.class, dummyServerSpec.fileName);
            String content = Resources.toString(url, Charsets.UTF_8);
            content = content.replaceAll("  ", "");

            return new RequestResult<>(content);
        } catch (IOException e) {
            LOG.error("xmllist() - cannot read file", e);
            throw new RuntimeException(e);
        }
    }

    public RequestResult<String> fileLogData(String command) {
        int lastSpace = command.lastIndexOf(" ");
        String columnSpec = command.substring(lastSpace + 1);

        String today = FORMATTER.print(new DateTime());

        String content = today + "_00:16:48 4.2\r\n" +
                today + "_01:19:21 5.2\r\n" +
                today + "_02:21:53 5.2\r\n" +
                today + "_03:24:26 6.2\r\n" +
                today + "_04:26:58 7.3\r\n" +
                today + "_05:32:03 8.2\r\n" +
                today + "_06:37:08 9.3\r\n" +
                today + "_07:39:41 8.3\r\n" +
                today + "_08:42:13 6.3\r\n" +
                today + "_09:44:46 5.3\r\n" +
                today + "_10:49:51 4.3\r\n" +
                today + "_11:52:23 3.3\r\n" +
                today + "_12:54:56 2.3\r\n" +
                today + "_13:57:28 1.3\r\n" +
                "#" + columnSpec;

        return new RequestResult<>(content);
    }

    @Override
    public RequestResult<Bitmap> requestBitmap(String relativePath) {
        LOG.error("requestBitmap() - get image from {}", relativePath);
        return new RequestResult<>(null, null);
    }
}
