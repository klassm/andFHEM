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
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.DeviceVisibility;
import li.klass.fhem.fhem.connection.DummyServerSpec;
import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.fhem.connection.ServerType;
import li.klass.fhem.service.intent.LicenseIntentService;
import li.klass.fhem.util.ApplicationProperties;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.AndFHEMApplication.PREMIUM_ALLOWED_FREE_CONNECTIONS;
import static li.klass.fhem.constants.PreferenceKeys.SELECTED_CONNECTION;
import static li.klass.fhem.fhem.connection.ServerType.FHEMWEB;

@Singleton
public class ConnectionService {
    public static final String DUMMY_DATA_ID = "-1";
    public static final String TEST_DATA_ID = "-2";
    public static final String MANAGEMENT_DATA_ID = "-3";
    private static final Gson GSON = new Gson();
    public static final String PREFERENCES_NAME = "fhemConnections";

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    LicenseIntentService licenseIntentService;

    private FHEMServerSpec getTestData(Context context) {
        FHEMServerSpec testData = null;
        if (LicenseIntentService.isDebug(context)) {
            testData = new DummyServerSpec(TEST_DATA_ID, "test.xml");
            testData.setName("TestData");
            testData.setServerType(ServerType.DUMMY);
        }
        return testData;
    }

    private FHEMServerSpec getDummyData() {
        FHEMServerSpec dummyData = new DummyServerSpec(DUMMY_DATA_ID, "dummyData.xml");
        dummyData.setName("DummyData");
        dummyData.setServerType(ServerType.DUMMY);
        return dummyData;
    }

    public void create(final String name, final ServerType serverType, final String username,
                       final String password, final String ip, final int port, final String url, final String alternateUrl,
                       final String clientCertificatePath, final String clientCertificatePassword, final Context context) {
        if (exists(name, context)) return;

        licenseIntentService.isPremium(new LicenseIntentService.IsPremiumListener() {
            @Override
            public void isPremium(boolean isPremium) {
                if (isPremium || getCountWithoutDummy(context) < PREMIUM_ALLOWED_FREE_CONNECTIONS) {

                    FHEMServerSpec server = new FHEMServerSpec(newUniqueId(context));

                    fillServerWith(name, server, serverType, username, password, ip, port, url, alternateUrl,
                            clientCertificatePath, clientCertificatePassword);

                    saveToPreferences(server, context);
                }
            }
        });

    }

    public boolean exists(String id, Context context) {
        return DUMMY_DATA_ID.equals(id) || TEST_DATA_ID.equals(id)
                || getPreferences(context).contains(id);
    }

    private int getCountWithoutDummy(Context context) {
        Map<String, ?> all = getPreferences(context).getAll();
        if (all == null) return 0;
        return all.size();
    }

    private String newUniqueId(Context context) {
        String id = null;
        while (id == null || exists(id, context) || DUMMY_DATA_ID.equals(id)
                || TEST_DATA_ID.equals(id) || MANAGEMENT_DATA_ID.equals(id)) {
            id = UUID.randomUUID().toString();
        }

        return id;
    }

    private void fillServerWith(String name, FHEMServerSpec server, ServerType serverType, String username,
                                String password, String ip, int port, String url, String alternateUrl,
                                String clientCertificatePath, String clientCertificatePassword) {
        server.setName(name);
        server.setServerType(serverType);
        server.setUsername(username);
        server.setPort(port);
        server.setPassword(password);
        server.setIp(ip);
        server.setUrl(url);
        server.setAlternateUrl(alternateUrl);
        server.setClientCertificatePath(clientCertificatePath);
        server.setClientCertificatePassword(clientCertificatePassword);
    }

    private void saveToPreferences(FHEMServerSpec server, Context context) {
        if (server.getServerType() == ServerType.DUMMY) return;

        String json = serialize(server);
        getPreferences(context).edit().putString(server.getId(), json).commit();
    }

    private SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
    }

    static String serialize(FHEMServerSpec serverSpec) {
        return GSON.toJson(serverSpec);
    }

    public boolean update(String id, String name, ServerType serverType, String username, String password,
                          String ip, int port, String url, String alternateUrl, String clientCertificatePath, String clientCertificatePassword, Context context) {

        FHEMServerSpec server = forId(id, context);
        if (server == null) return false;

        fillServerWith(name, server, serverType, username, password, ip, port, url, alternateUrl,
                clientCertificatePath, clientCertificatePassword);

        saveToPreferences(server, context);

        return true;
    }

    public FHEMServerSpec forId(String id, Context context) {
        if (DUMMY_DATA_ID.equals(id)) return getDummyData();
        FHEMServerSpec testData = getTestData(context);
        if (TEST_DATA_ID.equals(id) && testData != null) return testData;

        String json = getPreferences(context).getString(id, null);
        if (json == null) return null;
        return deserialize(json);
    }

    static FHEMServerSpec deserialize(String json) {
        return GSON.fromJson(json, FHEMServerSpec.class);
    }

    public boolean delete(String id, Context context) {
        if (!exists(id, context)) return false;

        getPreferences(context).edit().remove(id).commit();

        return true;
    }

    public ArrayList<FHEMServerSpec> listAll(Context context) {
        ArrayList<FHEMServerSpec> servers = newArrayList();

        SharedPreferences preferences = getPreferences(context);
        if (preferences == null) return servers;

        Map<String, ?> all = preferences.getAll();
        if (all == null) return servers;

        Collection<?> values = all.values();
        for (Object value : values) {
            String json = (String) value;
            FHEMServerSpec server = deserialize(json);

            servers.add(server);
        }

        servers.add(getDummyData());
        FHEMServerSpec testData = getTestData(context);
        if (testData != null) servers.add(testData);

        return servers;
    }

    public boolean mayShowInCurrentConnectionType(DeviceType deviceType, Context context) {
        DeviceVisibility visibility = deviceType.getVisibility();

        if (visibility == null) return true;

        ServerType serverType = getCurrentServer(context).getServerType();
        if (visibility == DeviceVisibility.NEVER) return false;

        ServerType showOnlyIn = visibility.getShowOnlyIn();
        return showOnlyIn == null || serverType == showOnlyIn;
    }

    public FHEMServerSpec getCurrentServer(Context context) {
        return forId(getSelectedId(context), context);
    }

    public String getSelectedId(Context context) {
        String id = applicationProperties.getStringSharedPreference(SELECTED_CONNECTION, DUMMY_DATA_ID, context);
        if (!exists(id, context)) id = DUMMY_DATA_ID;

        return id;
    }

    public void setSelectedId(String id, Context context) {
        if (!exists(id, context)) id = DUMMY_DATA_ID;
        applicationProperties.setSharedPreference(SELECTED_CONNECTION, id, context);
    }

    public int getPortOfSelectedConnection(Context context) {
        FHEMServerSpec spec = getCurrentServer(context);
        ServerType serverType = spec.getServerType();

        switch (serverType) {
            case TELNET:
                return spec.getPort();
            case DUMMY:
                return 0;
            case FHEMWEB:
                return getPortOfFHEMWEBSpec(spec);

            default:
                throw new IllegalArgumentException("unknown spec type: " + spec.getServerType());
        }
    }

    private int getPortOfFHEMWEBSpec(FHEMServerSpec spec) {
        checkArgument(spec.getServerType() == FHEMWEB);
        Pattern explicitPortPattern = Pattern.compile(":([\\d]+)");
        String url = spec.getUrl();
        Matcher matcher = explicitPortPattern.matcher(url);
        if (matcher.find()) {
            return Integer.valueOf(matcher.group(1));
        }

        if (url.startsWith("https://")) {
            return 443;
        }

        if (url.startsWith("http://")) {
            return 80;
        }

        return 0;
    }
}
