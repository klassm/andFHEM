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

package li.klass.fhem.service.connection;

import android.app.Activity;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.fhem.connection.DummyServerSpec;
import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.fhem.connection.ServerType;
import li.klass.fhem.license.LicenseManager;
import li.klass.fhem.util.ApplicationProperties;

import static li.klass.fhem.AndFHEMApplication.PREMIUM_ALLOWED_FREE_CONNECTIONS;
import static li.klass.fhem.constants.PreferenceKeys.SELECTED_CONNECTION;

public class ConnectionService {
    public static final ConnectionService INSTANCE = new ConnectionService();

    private static final Gson GSON = new Gson();

    private static final String PREFERENCES_NAME = "fhemConnections";
    public static final String DUMMY_DATA_ID = "-1";
    public static final String TEST_DATA_ID = "-2";
    public static final String MANAGEMENT_DATA_ID = "-3";

    private FHEMServerSpec dummyData;
    private DummyServerSpec testData;


    private ConnectionService() {
        initialiseDummyData();
    }

    private void initialiseDummyData() {
        dummyData = new DummyServerSpec(DUMMY_DATA_ID, "dummyData.xml");
        dummyData.setName("DummyData");
        dummyData.setServerType(ServerType.DUMMY);

        if (LicenseManager.INSTANCE.isDebug()) {
            testData = new DummyServerSpec(TEST_DATA_ID, "test.xml");
            testData.setName("TestData");
            testData.setServerType(ServerType.DUMMY);
        }
    }

    public boolean create(String name, ServerType serverType, String username, String password,
                          String ip, int port, String url) {
        if (exists(name)) return false;

        LicenseManager licenseManager = LicenseManager.INSTANCE;
        if (! licenseManager.isPro() && getCountWithoutDummy() >= PREMIUM_ALLOWED_FREE_CONNECTIONS) {
            return false;
        }

        FHEMServerSpec server = new FHEMServerSpec(newUniqueId());

        fillServerWith(name, server, serverType, username, password, ip, port, url);

        saveToPreferences(server);

        return true;
    }

    public boolean update(String id, String name, ServerType serverType, String username, String password,
                          String ip, int port, String url) {

        FHEMServerSpec server = forId(id);
        if (server == null) return false;

        fillServerWith(name, server, serverType, username, password, ip, port, url);

        saveToPreferences(server);

        return true;
    }

    private void fillServerWith(String name, FHEMServerSpec server, ServerType serverType, String username,
                                String password, String ip, int port, String url) {
        server.setName(name);
        server.setServerType(serverType);
        server.setUsername(username);
        server.setPort(port);
        server.setPassword(password);
        server.setIp(ip);
        server.setUrl(url);
    }

    public FHEMServerSpec forId(String id) {
        if (DUMMY_DATA_ID.equals(id)) return dummyData;
        if (TEST_DATA_ID.equals(id) && testData != null) return testData;

        String json = getPreferences().getString(id, null);
        if (json == null) return null;
        return deserialize(json);
    }

    public boolean delete(String id) {
        if (! exists(id)) return false;

        getPreferences().edit().remove(id).commit();

        return true;
    }

    private int getCountWithoutDummy() {
        Map<String, ?> all = getPreferences().getAll();
        if (all == null) return 0;
        return all.size();
    }

    public ArrayList<FHEMServerSpec> listAll() {
        ArrayList<FHEMServerSpec> servers = new ArrayList<FHEMServerSpec>();

        SharedPreferences preferences = getPreferences();
        if (preferences == null) return servers;

        Map<String, ?> all = preferences.getAll();
        if (all == null) return servers;

        Collection<?> values = all.values();
        for (Object value : values) {
            String json = (String) value;
            FHEMServerSpec server = deserialize(json);

            servers.add(server);
        }

        servers.add(dummyData);
        if (testData != null) servers.add(testData);

        return servers;
    }

    private String newUniqueId() {
        String id = null;
        while (id == null || exists(id) || DUMMY_DATA_ID.equals(id)
                || TEST_DATA_ID.equals(id) || MANAGEMENT_DATA_ID.equals(id)) {
            id = UUID.randomUUID().toString();
        }

        return id;
    }

    String serialize(FHEMServerSpec serverSpec) {
        return GSON.toJson(serverSpec);
    }

    FHEMServerSpec deserialize(String json) {
        return GSON.fromJson(json, FHEMServerSpec.class);
    }

    public boolean exists(String id) {
        return DUMMY_DATA_ID.equals(id) || TEST_DATA_ID.equals(id)
                || getPreferences().contains(id);
    }

    public boolean nameExists(String name) {
        for (FHEMServerSpec serverSpec : listAll()) {
            if (serverSpec.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private void saveToPreferences(FHEMServerSpec server) {
        if (server.getServerType() == ServerType.DUMMY) return;

        String json = serialize(server);
        getPreferences().edit().putString(server.getId(), json).commit();
    }

    private SharedPreferences getPreferences() {
        return AndFHEMApplication.getContext().getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
    }

    public void setSelectedId(String id) {
        if (! exists(id)) id = DUMMY_DATA_ID;
        ApplicationProperties.INSTANCE.setSharedPreference(SELECTED_CONNECTION, id);
    }

    public String getSelectedId() {
        ApplicationProperties applicationProperties = ApplicationProperties.INSTANCE;
        String id = applicationProperties.getStringSharedPreference(SELECTED_CONNECTION, DUMMY_DATA_ID);
        if (! exists(id)) id = DUMMY_DATA_ID;

        return id;
    }

    public FHEMServerSpec getCurrentServer() {
        return forId(getSelectedId());
    }
}
