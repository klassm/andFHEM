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

package li.klass.fhem.fhem.clientCert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * Class taken from http://stackoverflow.com/questions/13363940/how-to-make-client-certificate-authentication-from-android-4-1-with-apache-clien
 */
public class KeyChainProxy extends KeyStoreSpi {


    private String alias = null;
    private PrivateKey privateKey = null;
    private Certificate[] certChain = null;

    public KeyChainProxy(String alias, PrivateKey privateKey, Certificate[] certChain) {
        this.alias = alias;
        this.privateKey = privateKey;
        this.certChain = certChain;
    }

    @Override
    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        return privateKey;
    }

    @Override
    public Certificate[] engineGetCertificateChain(String alias) {
        return certChain;
    }

    @Override
    public Certificate engineGetCertificate(String alias) {
        return certChain[0];
    }

    @Override
    public Date engineGetCreationDate(String alias) {
        return new Date();
    }

    @Override
    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        throw new KeyStoreException("Not Implemented");

    }

    @Override
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        throw new KeyStoreException("Not Implemented");

    }

    @Override
    public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        throw new KeyStoreException("Not Implemented");

    }

    @Override
    public void engineDeleteEntry(String alias) throws KeyStoreException {
        throw new KeyStoreException("Not Implemented");

    }

    @Override
    public Enumeration<String> engineAliases() {
        List<String> list = new ArrayList<String>();
        list.add(alias);
        return Collections.enumeration(list);
    }

    @Override
    public boolean engineContainsAlias(String alias) {
        return alias != null && alias.equals(this.alias);
    }

    @Override
    public int engineSize() {
        return 1;
    }

    @Override
    public boolean engineIsKeyEntry(String alias) {
        return true;
    }

    @Override
    public boolean engineIsCertificateEntry(String alias) {
        return false;
    }

    @Override
    public String engineGetCertificateAlias(Certificate cert) {
        return null;
    }

    @Override
    public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {

    }

    @Override
    public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {

    }

}