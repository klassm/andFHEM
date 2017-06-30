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

import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine1;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine2;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.room.xmllist.DeviceNode;

import static li.klass.fhem.domain.core.DeviceFunctionality.USAGE;

@OverviewViewSettings(showState = false)
@SupportsWidget(MediumInformationWidgetView.class)
@SuppressWarnings("unused")
public class EC3000Device extends FhemDevice {
    @ShowField(description = ResourceIdMapper.energy_consumption, showInOverview = true)
    @XmllistAttribute("consumption")
    private String consumption;

    @ShowField(description = ResourceIdMapper.energy_power, showInOverview = true)
    @WidgetMediumLine1(description = ResourceIdMapper.energy_power)
    @XmllistAttribute("power")
    private String power;

    @WidgetMediumLine2(description = ResourceIdMapper.energy_consumption)
    private String widgetInfoLine;

    @ShowField(description = ResourceIdMapper.price)
    @XmllistAttribute("Euro")
    private String price;

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return USAGE;
    }

    @Override
    public void onChildItemRead(DeviceNode.DeviceNodeType type, String key, String value, DeviceNode node) {
        super.onChildItemRead(type, key, value, node);

        if (key.endsWith("lastRcv")) {
            setMeasured(value);
        }
    }

    @Override
    public void afterDeviceXMLRead(Context context) {
        super.afterDeviceXMLRead(context);
        widgetInfoLine = price + ", " + consumption;
    }

    public String getConsumption() {
        return consumption;
    }

    public String getPower() {
        return power;
    }

    public String getWidgetInfoLine() {
        return widgetInfoLine;
    }

    public String getPrice() {
        return price;
    }
}
