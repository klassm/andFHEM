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
import li.klass.fhem.appwidget.annotation.WidgetTemperatureAdditionalField;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

@FloorplanViewSettings(showState = true)
@SupportsWidget(TemperatureWidgetView.class)
@SuppressWarnings("unused")
public class PIDDevice extends Device<PIDDevice> {

    @ShowField(description = R.string.temperature, showInOverview = true)
    @WidgetTemperatureField
    private String temperature;

    @ShowField(description = R.string.desiredTemperature)
    private double desiredTemperature;

    @ShowField(description = R.string.delta, showInOverview = true)
    @WidgetTemperatureAdditionalField(description = R.string.delta)
    private String delta;

    public static final double MINIMUM_TEMPERATURE = 0;
    public static final double MAXIMUM_TEMPERATURE = 40;

    public void readSTATE(String value) {
        String content = value.replaceAll("[\\(\\)]", "").replaceAll("  ", "");
        String[] parts = content.split(" ");

        if (parts.length == 1) {
            return;
        }

        temperature = ValueDescriptionUtil.appendTemperature(parts[0]);
        delta = parts[2];
    }

    public void readDESIRED(String value) {
        desiredTemperature = ValueExtractUtil.extractLeadingDouble(value);
    }

    public String getTemperature() {
        return temperature;
    }

    public void setDesiredTemperature(double desiredTemperature) {
        this.desiredTemperature = desiredTemperature;
    }

    public double getDesiredTemperature() {
        return desiredTemperature;
    }

    public String getDelta() {
        return delta;
    }
}
