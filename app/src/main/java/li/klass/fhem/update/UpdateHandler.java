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
        if (!application.isUpdate()) return;

        ApplicationProperties applicationProperties = ApplicationProperties.INSTANCE;


        handleConnectionUpdate(applicationProperties);
    }

    private void handleConnectionUpdate(ApplicationProperties applicationProperties) {
        String selectedConnection = applicationProperties.getStringSharedPreference(CONNECTION_TYPE, null);
        ConnectionService connectionService = ConnectionService.INSTANCE;

        applicationProperties.deleteSharedPreference(CONNECTION_TYPE);

        String fhemwebUrl = applicationProperties.getStringSharedPreference(FHEMWEB_URL, null);
        String fhemwebUser = applicationProperties.getStringSharedPreference(FHEMWEB_USERNAME, "");
        String fhemwebPassword = applicationProperties.getStringSharedPreference(FHEMWEB_PASSWORD, "");

        if (! StringUtil.isBlank(fhemwebUrl)) {
            if (! connectionService.nameExists("FHEMWEB")) {
                if (connectionService.create("FHEMWEB", ServerType.FHEMWEB, fhemwebUser, fhemwebPassword,
                        null, 0, fhemwebUrl)) {
                    applicationProperties.deleteSharedPreference(FHEMWEB_URL);
                    applicationProperties.deleteSharedPreference(FHEMWEB_PASSWORD);
                    applicationProperties.deleteSharedPreference(FHEMWEB_USERNAME);
                }
            }
        }

        String telnetIp = applicationProperties.getStringSharedPreference(TELNET_URL, "");
        String telnetPort = applicationProperties.getStringSharedPreference(TELNET_PORT, "0");
        String telnetPassword = applicationProperties.getStringSharedPreference(TELNET_PASSWORD, "");

        if (!StringUtil.isBlank(telnetIp)) {

            Integer port;
            try {
                port = Integer.valueOf(telnetPort);
            } catch (Exception e) {
                port = 0;
            }
            if (! connectionService.nameExists("Telnet")) {
                if (connectionService.create("Telnet", ServerType.TELNET, null, telnetPassword,
                        telnetIp, port, null)) {
                    applicationProperties.deleteSharedPreference(TELNET_URL);
                    applicationProperties.deleteSharedPreference(TELNET_PORT);
                    applicationProperties.deleteSharedPreference(TELNET_PASSWORD);
                }
            }
        }

        try {


            ArrayList<FHEMServerSpec> allServers = connectionService.listAll();
            if (selectedConnection != null) {
                ServerType serverType = ServerType.valueOf(selectedConnection.toUpperCase());
                for (FHEMServerSpec server : allServers) {
                    if (server.getServerType() == serverType) {
                        connectionService.setSelectedId(server.getId());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
