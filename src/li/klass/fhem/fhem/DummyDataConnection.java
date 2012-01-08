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

import android.util.Log;
import li.klass.fhem.util.CloseableUtil;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DummyDataConnection implements FHEMConnection {
    public static final DummyDataConnection INSTANCE = new DummyDataConnection();

    private DummyDataConnection() {
    }
    
    @Override
    public String xmllist() {

        InputStream inputStream = null;
        try {
            inputStream = DummyDataConnection.class.getResource("test.xml").openStream();
            String content = IOUtils.toString(inputStream);
            content = content.replaceAll("\n", "");
            content = content.replaceAll("  ", "");
            return content;
        } catch (IOException e) {
            Log.e(DummyDataConnection.class.getName(), "cannot read file", e);
            throw new RuntimeException(e);
        } finally {
            CloseableUtil.close(inputStream);
        }
    }

    @Override
    public String fileLogData(String logName, Date fromDate, Date toDate, String columnSpec) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = simpleDateFormat.format(new Date());

        return today + "_00:16:48 4.2" +
                today + "_01:19:21 5.2" +
                today + "_02:21:53 5.2" +
                today + "_03:24:26 6.2" +
                today + "_04:26:58 7.3" +
                today + "_05:32:03 8.2" +
                today + "_06:37:08 9.3" +
                today + "_07:39:41 8.3" +
                today + "_08:42:13 6.3" +
                today + "_09:44:46 5.3" +
                today + "_10:49:51 4.3" +
                today + "_11:52:23 3.3" +
                today + "_12:54:56 2.3" +
                today + "_13:57:28 1.3" +
                "#" + columnSpec;
    }

    @Override
    public void executeCommand(String command) {
        Log.e(DummyDataConnection.class.getName(), "execute command " + command);
    }
}
