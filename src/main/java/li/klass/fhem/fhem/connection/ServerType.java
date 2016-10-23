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

package li.klass.fhem.fhem.connection;

import li.klass.fhem.fhem.DummyDataConnection;
import li.klass.fhem.fhem.FHEMConnection;
import li.klass.fhem.fhem.FHEMWEBConnection;
import li.klass.fhem.fhem.TelnetConnection;
import li.klass.fhem.util.ApplicationProperties;

public enum ServerType {
    DUMMY(new ConnectionProvider() {
        @Override
        public FHEMConnection getFor(FHEMServerSpec fhemServerSpec, ApplicationProperties applicationProperties) {
            return new DummyDataConnection(fhemServerSpec, applicationProperties);
        }
    }),
    FHEMWEB(new ConnectionProvider() {
        @Override
        public FHEMConnection getFor(FHEMServerSpec fhemServerSpec, ApplicationProperties applicationProperties) {
            return new FHEMWEBConnection(fhemServerSpec, applicationProperties);
        }
    }),
    TELNET(new ConnectionProvider() {
        @Override
        public FHEMConnection getFor(FHEMServerSpec fhemServerSpec, ApplicationProperties applicationProperties) {
            return new TelnetConnection(fhemServerSpec, applicationProperties);
        }
    });

    private final ConnectionProvider provider;

    ServerType(ConnectionProvider provider) {
        this.provider = provider;
    }

    public FHEMConnection getConnectionFor(FHEMServerSpec fhemServerSpec, ApplicationProperties applicationProperties) {
        return provider.getFor(fhemServerSpec, applicationProperties);
    }

    private interface ConnectionProvider {
        FHEMConnection getFor(FHEMServerSpec fhemServerSpec, ApplicationProperties applicationProperties);
    }
}
