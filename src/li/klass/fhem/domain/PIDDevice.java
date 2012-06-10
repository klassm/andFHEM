/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.domain;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.TemperatureWidgetView;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ValueDescriptionUtil;
import org.w3c.dom.NamedNodeMap;

@FloorplanViewSettings(showState = true)
@SupportsWidget(TemperatureWidgetView.class)
public class PIDDevice extends Device<PIDDevice> {
    @ShowField(description = R.string.temperature, showInOverview = true)
    @WidgetTemperatureField
    private String temperature;
    @ShowField(description = R.string.delta, showInOverview = true)
    private String delta;

    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equalsIgnoreCase("STATE")) {
            String content = nodeContent.replaceAll("[\\(\\)]", "").replaceAll("  ", "");
            String[] parts = content.split(" ");
            
            temperature = ValueDescriptionUtil.appendTemperature(parts[0]);
            delta = parts[2];
        }
    }

    public String getTemperature() {
        return temperature;
    }

    public String getDelta() {
        return delta;
    }
}
