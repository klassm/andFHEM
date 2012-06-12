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

import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ValueUtil;
import org.w3c.dom.NamedNodeMap;

@SuppressWarnings("unused")
@FloorplanViewSettings(showState = true)
@SupportsWidget(TemperatureWidgetView.class)
public class OwtempDevice extends Device<OwtempDevice> {

    @ShowField(description = R.string.temperature, showInOverview = true)
    @WidgetTemperatureField
    private String temperature;

    @ShowField(description = R.string.warnings, showInOverview = true)
    private String warnings;

    @Override
    public void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TEMPERATURE")) {
            this.temperature = ValueUtil.formatTemperature(nodeContent);
        } else if (keyValue.equals("WARNINGS")) {
            this.warnings = nodeContent;
            measured = attributes.getNamedItem("measured").getNodeValue();
        }
    }

    public String getTemperature() {
        return temperature;
    }

    public String getWarnings() {
        return warnings;
    }
}
