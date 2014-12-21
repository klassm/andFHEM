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

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * A custom X509TrustManager implementation that trusts a specified server certificate in addition
 * to those that are in the system TrustStore.
 * Also handles an out-of-order certificate chain, as is often produced by Apache's mod_ssl
 */
public class ClientCertTrustManager implements X509TrustManager {

    private final X509TrustManager originalX509TrustManager;
    private final KeyStore trustStore;

    /**
     * @param trustStore A KeyStore containing the server certificate that should be trusted
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    public ClientCertTrustManager(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
        this.trustStore = trustStore;

        TrustManagerFactory originalTrustManagerFactory = TrustManagerFactory.getInstance("X509");
        originalTrustManagerFactory.init((KeyStore) null);

        TrustManager[] originalTrustManagers = originalTrustManagerFactory.getTrustManagers();
        originalX509TrustManager = (X509TrustManager) originalTrustManagers[0];
    }

    /**
     * No-op. Never invoked by client, only used in server-side implementations
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    /**
     * No-op. Never invoked by client, only used in server-side implementations
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
    }

    /**
     * Given the partial or complete certificate chain provided by the peer,
     * build a certificate path to a trusted root and return if it can be validated and is trusted
     * for client SSL authentication based on the authentication type. The authentication type is
     * determined by the actual certificate used. For instance, if RSAPublicKey is used, the authType should be "RSA".
     * Checking is case-sensitive.
     * Defers to the default trust manager first, checks the cert supplied in the ctor if that fails.
     *
     * @param chain    the server's certificate chain
     * @param authType the authentication type based on the client certificate
     * @throws java.security.cert.CertificateException
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
        try {
            originalX509TrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException originalException) {
            try {
                X509Certificate[] reorderedChain = reorderCertificateChain(chain);
                CertPathValidator validator = CertPathValidator.getInstance("PKIX");
                CertificateFactory factory = CertificateFactory.getInstance("X509");
                CertPath certPath = factory.generateCertPath(Arrays.asList(reorderedChain));
                PKIXParameters params = new PKIXParameters(trustStore);
                params.setRevocationEnabled(false);
                validator.validate(certPath, params);
            } catch (Exception ex) {
                throw originalException;
            }
        }

    }

    /**
     * Puts the certificate chain in the proper order, to deal with out-of-order
     * certificate chains as are sometimes produced by Apache's mod_ssl
     *
     * @param chain the certificate chain, possibly with bad ordering
     * @return the re-ordered certificate chain
     */
    private X509Certificate[] reorderCertificateChain(X509Certificate[] chain) {

        X509Certificate[] reorderedChain = new X509Certificate[chain.length];
        List<X509Certificate> certificates = Arrays.asList(chain);

        int position = chain.length - 1;
        X509Certificate rootCert = findRootCert(certificates);
        reorderedChain[position] = rootCert;

        X509Certificate cert = rootCert;
        while ((cert = findSignedCert(cert, certificates)) != null && position > 0) {
            reorderedChain[--position] = cert;
        }

        return reorderedChain;
    }

    /**
     * A helper method for certificate re-ordering.
     * Finds the root certificate in a possibly out-of-order certificate chain.
     *
     * @param certificates the certificate change, possibly out-of-order
     * @return the root certificate, if any, that was found in the list of certificates
     */
    private X509Certificate findRootCert(List<X509Certificate> certificates) {
        X509Certificate rootCert = null;

        for (X509Certificate cert : certificates) {
            X509Certificate signer = findSigner(cert, certificates);
            if (signer == null || signer.equals(cert)) { // no signer present, or self-signed
                rootCert = cert;
                break;
            }
        }

        return rootCert;
    }

    /**
     * A helper method for certificate re-ordering.
     * Finds the first certificate in the list of certificates that is signed by the sigingCert.
     */
    private X509Certificate findSignedCert(X509Certificate signingCert, List<X509Certificate> certificates) {
        X509Certificate signed = null;

        for (X509Certificate cert : certificates) {
            Principal signingCertSubjectDN = signingCert.getSubjectDN();
            Principal certIssuerDN = cert.getIssuerDN();
            if (certIssuerDN.equals(signingCertSubjectDN) && !cert.equals(signingCert)) {
                signed = cert;
                break;
            }
        }

        return signed;
    }

    /**
     * A helper method for certificate re-ordering.
     * Finds the certificate in the list of certificates that signed the signedCert.
     */
    private X509Certificate findSigner(X509Certificate signedCert, List<X509Certificate> certificates) {
        X509Certificate signer = null;

        for (X509Certificate cert : certificates) {
            Principal certSubjectDN = cert.getSubjectDN();
            Principal issuerDN = signedCert.getIssuerDN();
            if (certSubjectDN.equals(issuerDN)) {
                signer = cert;
                break;
            }
        }

        return signer;
    }
}