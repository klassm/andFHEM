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

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.util.ApplicationProperties;

@Singleton
public class DataConnectionSwitch {

    @Inject
    ConnectionService connectionService;

    @Inject
    ApplicationProperties applicationProperties;

    public FHEMConnection getCurrentProvider() {
        FHEMServerSpec selectedServer = connectionService.getCurrentServer();
        FHEMConnection currentConnection = selectedServer.getServerType().getConnection();
        currentConnection.setApplicationProperties(applicationProperties);

        FHEMServerSpec currentServer = currentConnection.getServer();
        if (currentServer == null || !currentServer.equals(selectedServer)) {
            currentConnection.setServer(selectedServer);
        }

        return currentConnection;
    }
}
