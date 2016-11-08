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

import android.content.Context;

import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.fhem.connection.DummyServerSpec;
import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.util.ApplicationProperties;

@Singleton
public class DataConnectionSwitch {

    @Inject
    ConnectionService connectionService;

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    public DataConnectionSwitch() {
    }

    public Optional<FHEMConnection> getProviderFor(Context context, Optional<String> connectionId) {
        if (!connectionService.exists(connectionId, context)) {
            return Optional.absent();
        }
        FHEMServerSpec serverSpec = getSpecFor(context, connectionId);
        FHEMConnection currentConnection = serverSpec.getServerType().getConnectionFor(serverSpec, applicationProperties);
        return Optional.of(currentConnection);
    }

    private FHEMServerSpec getSpecFor(Context context, Optional<String> connectionId) {
        return connectionService.getServerFor(context, connectionId);
    }

    public FHEMConnection getProviderFor(Context context) {
        return getProviderFor(context, Optional.absent()).get();
    }

    public boolean isDummyDataActive(Context context) {
        return getSpecFor(context, Optional.absent()) instanceof DummyServerSpec;
    }
}
