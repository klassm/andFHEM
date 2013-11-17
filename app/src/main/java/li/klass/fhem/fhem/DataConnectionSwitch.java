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

import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.service.connection.ConnectionService;

public class DataConnectionSwitch {
    public static final DataConnectionSwitch INSTANCE = new DataConnectionSwitch();

    public static final String CONNECTION_TYPE = "CONNECTION_TYPE";

    private DataConnectionSwitch() {
    }

    public FHEMConnection getCurrentProvider() {
        FHEMConnection currentConnection;
        FHEMServerSpec currentServer = ConnectionService.INSTANCE.getCurrentServer();

        switch (currentServer.getServerType()) {
            case TELNET:
                currentConnection = TelnetConnection.INSTANCE;
                break;
            case FHEMWEB:
                currentConnection = FHEMWEBConnection.INSTANCE;
                break;
            case DUMMY:
                currentConnection = DummyDataConnection.INSTANCE;
                break;
            default:
                throw new IllegalArgumentException("don't know how to handle " +
                        currentServer.getServerType().name());
        }

        currentConnection.setServer(currentServer);

        Log.d(DataConnectionSwitch.class.getName(), "current connection provider: " +
                currentConnection.getClass().getName());

        return currentConnection;
    }
}
