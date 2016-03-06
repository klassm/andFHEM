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

package li.klass.fhem.ssl;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Locale;

import de.duenndns.ssl.MemorizingTrustManager;

public class AndFHEMMemorizingTrustManager extends MemorizingTrustManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AndFHEMMemorizingTrustManager.class);

    public AndFHEMMemorizingTrustManager(Context context) {
        super(context);

    }

    public boolean checkCertificate(X509Certificate certificate, String hostname) {
        try {
            return certificate.equals(appKeyStore.getCertificate(hostname.toLowerCase(Locale.US)))
                    || interactHostname(certificate, hostname);
        } catch (KeyStoreException e) {
            LOGGER.error("error while checking certificate", e);
            return false;
        }
    }
}
