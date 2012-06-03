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

package li.klass.fhem.domain;

import android.content.Context;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import org.w3c.dom.NamedNodeMap;

@SuppressWarnings("unused")
@FloorplanViewSettings(showState = true)
public class WOLDevice extends Device<WOLDevice> {

    @ShowField(description = R.string.state, showInOverview = true)
    private String isRunning;
    @ShowField(description = R.string.ip)
    private String ip;
    @ShowField(description = R.string.mac)
    private String mac;

    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("ISRUNNING")) {
            Context context = AndFHEMApplication.getContext();
            int isRunningId = Boolean.valueOf(nodeContent) ? R.string.on : R.string.off;
            isRunning = context.getString(isRunningId);
            measured = attributes.getNamedItem("measured").getNodeValue();
        } else if (keyValue.equals("IP")) {
            ip = nodeContent;
        } else if (keyValue.equals("MAC")) {
            mac = nodeContent.toUpperCase();
        }
    }

    public String isRunning() {
        return isRunning;
    }

    public String getIp() {
        return ip;
    }

    public String getMac() {
        return mac;
    }
}
