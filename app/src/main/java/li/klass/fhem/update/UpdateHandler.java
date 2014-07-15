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

package li.klass.fhem.update;

import android.util.Log;

import java.util.ArrayList;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.fhem.connection.ServerType;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.StringUtil;

import static li.klass.fhem.fhem.DataConnectionSwitch.CONNECTION_TYPE;
import static li.klass.fhem.fhem.FHEMWEBConnection.FHEMWEB_PASSWORD;
import static li.klass.fhem.fhem.FHEMWEBConnection.FHEMWEB_URL;
import static li.klass.fhem.fhem.FHEMWEBConnection.FHEMWEB_USERNAME;
import static li.klass.fhem.fhem.TelnetConnection.TELNET_PASSWORD;
import static li.klass.fhem.fhem.TelnetConnection.TELNET_PORT;
import static li.klass.fhem.fhem.TelnetConnection.TELNET_URL;

public class UpdateHandler {
    public static final UpdateHandler INSTANCE = new UpdateHandler();

    private UpdateHandler() {
    }

    private static final String TAG = UpdateHandler.class.getName();

    public void onApplicationUpdate() {
        AndFHEMApplication application = AndFHEMApplication.INSTANCE;
        if (!application.isUpdate()) {
            Log.i(UpdateHandler.class.getName(), "not an update");
        }
    }
}
