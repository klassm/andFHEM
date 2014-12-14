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

package li.klass.fhem.service.intent;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.util.ArrayList;

import javax.inject.Inject;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.fhem.connection.ServerType;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.util.StringUtil;

import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_CLIENT_CERTIFICATE_PASSWORD;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_CLIENT_CERTIFICATE_PATH;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ENABLE_CLIENT_CERTIFICATE;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_IP;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_PASSWORD;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_PORT;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_SERVER_CERTIFICATE_PATH;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_TYPE;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_URL;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_USERNAME;
import static li.klass.fhem.constants.ResultCodes.SUCCESS;

public class ConnectionsIntentService extends ConvenientIntentService {

    @Inject
    ConnectionService connectionService;

    public ConnectionsIntentService() {
        super(ConnectionsIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        String action = intent.getAction();

        if (Actions.CONNECTIONS_LIST.equals(action)) {
            ArrayList<FHEMServerSpec> serverSpecs = connectionService.listAll();

            Bundle bundle = new Bundle();
            bundle.putSerializable(CONNECTION_LIST, serverSpecs);
            bundle.putString(CONNECTION_ID, connectionService.getSelectedId());
            sendResult(resultReceiver, SUCCESS, bundle);
        } else if (Actions.CONNECTION_CREATE.equals(action)
                || Actions.CONNECTION_UPDATE.equals(action)) {

            String id = intent.getStringExtra(CONNECTION_ID);
            String name = intent.getStringExtra(CONNECTION_NAME);
            ServerType serverType = ServerType.valueOf(intent.getStringExtra(CONNECTION_TYPE));
            String url = intent.getStringExtra(CONNECTION_URL);
            String username = intent.getStringExtra(CONNECTION_USERNAME);
            String password = intent.getStringExtra(CONNECTION_PASSWORD);
            String ip = intent.getStringExtra(CONNECTION_IP);
            String clientCertificatePath = intent.getStringExtra(CONNECTION_CLIENT_CERTIFICATE_PATH);
            String clientCertificatePassword = intent.getStringExtra(CONNECTION_CLIENT_CERTIFICATE_PASSWORD);
            String serverCertificatePath = intent.getStringExtra(CONNECTION_SERVER_CERTIFICATE_PATH);
            boolean clientCertificateEnabled = intent.getBooleanExtra(CONNECTION_ENABLE_CLIENT_CERTIFICATE, false);

            String portString = intent.getStringExtra(CONNECTION_PORT);
            if (StringUtil.isBlank(portString)) portString = "0";
            int port = Integer.valueOf(portString);

            if (Actions.CONNECTION_CREATE.equals(action)) {
                connectionService.create(name, serverType, username,
                        password, ip, port, url, clientCertificatePath, serverCertificatePath,
                        clientCertificateEnabled, clientCertificatePassword);
            } else {
                connectionService.update(id, name, serverType, username, password, ip,
                        port, url, clientCertificatePath, serverCertificatePath,
                        clientCertificateEnabled, clientCertificatePassword);
            }

            sendChangedBroadcast();

            return STATE.SUCCESS;
        } else if (Actions.CONNECTION_GET.equals(action)) {
            String id = intent.getStringExtra(CONNECTION_ID);
            sendSingleExtraResult(resultReceiver, ResultCodes.SUCCESS, CONNECTION,
                    connectionService.forId(id));
        } else if (Actions.CONNECTION_DELETE.equals(action)) {
            String id = intent.getStringExtra(CONNECTION_ID);
            connectionService.delete(id);

            sendChangedBroadcast();
            return STATE.SUCCESS;
        } else if (Actions.CONNECTION_SET_SELECTED.equals(action)) {
            String currentlySelected = connectionService.getSelectedId();
            String id = intent.getStringExtra(CONNECTION_ID);

            if (currentlySelected.equals(id)) return STATE.SUCCESS;

            connectionService.setSelectedId(id);

            Intent updateIntent = new Intent(Actions.DO_UPDATE);
            updateIntent.putExtra(BundleExtraKeys.DO_REFRESH, true);
            sendBroadcast(updateIntent);

            return STATE.SUCCESS;
        } else if (Actions.CONNECTION_GET_SELECTED.equals(action)) {
            Bundle bundle = new Bundle();
            String selectedId = connectionService.getSelectedId();
            bundle.putString(CONNECTION_ID, selectedId);
            bundle.putSerializable(CONNECTION, connectionService.forId(selectedId));

            sendResult(resultReceiver, SUCCESS, bundle);
        }
        return STATE.DONE;
    }

    private void sendChangedBroadcast() {
        Intent changedIntent = new Intent(Actions.CONNECTIONS_CHANGED);
        sendBroadcast(changedIntent);
    }
}
