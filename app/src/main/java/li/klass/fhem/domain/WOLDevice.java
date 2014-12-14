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

import org.w3c.dom.NamedNodeMap;

import java.util.Locale;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine1;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine2;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.DetailViewSettings;
import li.klass.fhem.domain.genericview.ShowField;

@SuppressWarnings("unused")
@SupportsWidget(MediumInformationWidgetView.class)
@DetailViewSettings(showState = false)
public class WOLDevice extends Device<WOLDevice> {

    @ShowField(description = ResourceIdMapper.state, showInOverview = true)
    @WidgetMediumLine1
    private String isRunning;
    @ShowField(description = ResourceIdMapper.ip)
    @WidgetMediumLine2
    private String ip;
    @ShowField(description = ResourceIdMapper.mac)
    private String mac;

    private String shutdownCommand;

    public void readISRUNNING(String value, NamedNodeMap attributes) {
        Context context = AndFHEMApplication.getContext();
        int isRunningId = Boolean.valueOf(value) ? R.string.on : R.string.off;
        isRunning = context.getString(isRunningId);
        String measured = attributes.getNamedItem("measured").getNodeValue();
        setMeasured(measured);
    }

    public void readSHUTDOWNCMD(String value) {
        this.shutdownCommand = value;
    }

    public void readIP(String value) {
        ip = value;
    }

    public void readMAC(String value) {
        mac = value.toUpperCase(Locale.getDefault());
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

    public String getShutdownCommand() {
        return shutdownCommand;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.NETWORK;
    }
}
