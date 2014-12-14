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

import org.w3c.dom.NamedNodeMap;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine1;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine2;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;

import static li.klass.fhem.domain.core.DeviceFunctionality.USAGE;
import static li.klass.fhem.util.ValueDescriptionUtil.append;
import static li.klass.fhem.util.ValueDescriptionUtil.appendKWh;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;

@OverviewViewSettings(showState = false)
@SupportsWidget(MediumInformationWidgetView.class)
@SuppressWarnings("unused")
public class EC3000Device extends Device<EC3000Device> {
    @ShowField(description = ResourceIdMapper.energyConsumption, showInOverview = true)
    private String consumption;

    @ShowField(description = ResourceIdMapper.energyPower, showInOverview = true)
    @WidgetMediumLine1(description = ResourceIdMapper.energyPower)
    private String power;

    @WidgetMediumLine2(description = ResourceIdMapper.energyConsumption)
    private String widgetInfoLine;

    @ShowField(description = ResourceIdMapper.price)
    private String price;

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return USAGE;
    }

    public void readCONSUMPTION(String value) {
        this.consumption = appendKWh(value);
    }

    public void readPOWER(String value) {
        this.power = append(value, "W");
    }

    public void readEURO(String value) {
        this.price = append(extractLeadingDouble(value, 2), "€");
    }

    @Override
    public void readSTATE(String tagName, NamedNodeMap attributes, String value) {
        super.readSTATE(tagName, attributes, append(value, "W"));
    }

    @Override
    public void onChildItemRead(String tagName, String key, String value, NamedNodeMap attributes) {
        super.onChildItemRead(tagName, key, value, attributes);

        if (key.endsWith("lastRcv")) {
            setMeasured(value);
        }
    }

    @Override
    public void afterDeviceXMLRead() {
        super.afterDeviceXMLRead();
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
