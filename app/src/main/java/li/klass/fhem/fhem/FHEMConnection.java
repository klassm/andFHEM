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

import android.graphics.Bitmap;

import li.klass.fhem.constants.PreferenceKeys;
import li.klass.fhem.error.ErrorHolder;
import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.util.ApplicationProperties;

public abstract class FHEMConnection {

    public static final int CONNECTION_TIMEOUT_DEFAULT_SECONDS = 4;

    protected FHEMServerSpec serverSpec;
    protected ApplicationProperties applicationProperties;

    public abstract RequestResult<String> executeCommand(String command);

    public abstract RequestResult<Bitmap> requestBitmap(String relativePath);

    public FHEMServerSpec getServer() {
        return serverSpec;
    }

    public void setServer(FHEMServerSpec serverSpec) {
        this.serverSpec = serverSpec;
        onSetServerSpec();
    }

    protected void onSetServerSpec() {
    }

    protected void setErrorInErrorHolderFor(Exception e, String host, String command) {
        String text = "Error while accessing '" + host + "' with command '" + command + "'\r\n" +
                serverSpec.toString() + "\r\n";

        ErrorHolder.setError(e, text);
    }

    protected int getConnectionTimeoutMilliSeconds() {
        return 1000 * applicationProperties.getIntegerSharedPreference(
                PreferenceKeys.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT_DEFAULT_SECONDS
        );
    }

    public void setApplicationProperties(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }
}
