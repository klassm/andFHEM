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

import android.support.annotation.NonNull;

import com.google.common.base.Predicate;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import static com.google.common.base.Strings.isNullOrEmpty;

public class FHEMServerSpec implements Comparable<FHEMServerSpec>, Serializable {
    private final String id;
    private String name;
    private String password;
    private String ip;
    private int port;
    private String url;
    private String alternateUrl;
    private String username;
    private String clientCertificatePath;
    private String clientCertificatePassword;
    private ServerType serverType;

    public FHEMServerSpec(String id) {
        this.id = id;
    }

    @NonNull
    public static Predicate<FHEMServerSpec> notInstanceOf(final Class<? extends FHEMServerSpec> clazz) {
        return new Predicate<FHEMServerSpec>() {
            @Override
            public boolean apply(FHEMServerSpec input) {
                return input != null && !(clazz.isAssignableFrom(input.getClass()));
            }
        };
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlternateUrl() {
        return alternateUrl;
    }

    public void setAlternateUrl(String url) {
        this.alternateUrl = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClientCertificatePath() {
        return clientCertificatePath;
    }

    public void setClientCertificatePath(String clientCertificatePath) {
        this.clientCertificatePath = clientCertificatePath;
    }

    public String getClientCertificatePassword() {
        return clientCertificatePassword;
    }

    public void setClientCertificatePassword(String clientCertificatePassword) {
        this.clientCertificatePassword = clientCertificatePassword;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public void setServerType(ServerType serverType) {
        this.serverType = serverType;
    }

    public String getId() {
        return id;
    }

    @Override
    public int compareTo(@NotNull FHEMServerSpec fhemServerSpec) {
        return name.compareTo(fhemServerSpec.name);
    }

    @Override
    @SuppressWarnings("RedundantIfStatement")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FHEMServerSpec that = (FHEMServerSpec) o;

        if (port != that.port) return false;
        if (alternateUrl != null ? !alternateUrl.equals(that.alternateUrl) : that.alternateUrl != null)
            return false;
        if (clientCertificatePassword != null ? !clientCertificatePassword.equals(that.clientCertificatePassword) : that.clientCertificatePassword != null)
            return false;
        if (clientCertificatePath != null ? !clientCertificatePath.equals(that.clientCertificatePath) : that.clientCertificatePath != null)
            return false;
        if (ip != null ? !ip.equals(that.ip) : that.ip != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null)
            return false;
        if (serverType != that.serverType) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (alternateUrl != null ? alternateUrl.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (clientCertificatePath != null ? clientCertificatePath.hashCode() : 0);
        result = 31 * result + (clientCertificatePassword != null ? clientCertificatePassword.hashCode() : 0);
        result = 31 * result + serverType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FHEMServerSpec{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", password='" + (isNullOrEmpty(password) ? "empty" : "*****") + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", url='" + url + '\'' +
                ", alternateUrl='" + alternateUrl + '\'' +
                ", username='" + username + '\'' +
                ", clientCertificatePath='" + clientCertificatePath + '\'' +
                ", clientCertificatePassword='" + clientCertificatePassword + '\'' +
                ", serverType=" + serverType +
                '}';
    }

    public boolean canRetry() {
        return !isNullOrEmpty(alternateUrl);
    }
}
