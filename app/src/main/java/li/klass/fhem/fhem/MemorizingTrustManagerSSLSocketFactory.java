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
import android.util.Base64;

import com.google.common.io.CharStreams;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.duenndns.ssl.MemorizingTrustManager;
import li.klass.fhem.util.CloseableUtil;

import static com.google.common.base.Objects.firstNonNull;

public class MemorizingTrustManagerSSLSocketFactory extends SSLSocketFactory {
    SSLContext sslContext = null;

    public MemorizingTrustManagerSSLSocketFactory(KeyStore truststore,File clientCertificate,
                                                  String clientCertificatePassword, Context context) throws Exception {
        super(truststore);
        sslContext = SSLContext.getInstance("TLS");
        MemorizingTrustManager mtm = new MemorizingTrustManager(context);
        KeyManager[] clientKeys = null;
        if(clientCertificate != null && clientCertificate.exists()) {
            final KeyStore keyStore = loadPKCS12KeyStore(clientCertificate, clientCertificatePassword);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(keyStore, firstNonNull(clientCertificatePassword, "").toCharArray());
            clientKeys = kmf.getKeyManagers();
        }
        sslContext.init(clientKeys, new X509TrustManager[]{mtm}, new java.security.SecureRandom());
        setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }

    /**
     * Produces a KeyStore from a PKCS12 (.p12) certificate file, typically the client certificate
     *
     * @param certificateFile    A file containing the client certificate
     * @param clientCertPassword Password for the certificate
     * @return A KeyStore containing the certificate from the certificateFile
     * @throws Exception
     */
    private KeyStore loadPKCS12KeyStore(File certificateFile, String clientCertPassword) throws Exception {
        KeyStore keyStore = null;
        FileInputStream fis = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            fis = new FileInputStream(certificateFile);
            keyStore.load(fis, clientCertPassword.toCharArray());
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                // ignore
            }
        }
        return keyStore;
    }
}
