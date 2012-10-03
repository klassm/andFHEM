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
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ValueDescriptionUtil;

@FloorplanViewSettings(showState = true)
@SupportsWidget(TemperatureWidgetView.class)
@SuppressWarnings("unused")
public class TRXWeatherDevice extends Device<TRXWeatherDevice> {

    @WidgetTemperatureField
    @ShowField(description = R.string.temperature, showInOverview = true)
    private String temperature;

    @ShowField(description = R.string.battery, showInOverview = true)
    private String battery;

    @ShowField(description = R.string.humidity, showInOverview = true)
    private String humidity;

    @ShowField(description = R.string.dewpoint, showInOverview = false)
    private String dewpoint;

    public void readTEMPERATURE(String value) {
        this.temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readBATTERY(String value) {
        this.battery = value;
    }

    public void readHUMIDITY(String value) {
        this.humidity = ValueDescriptionUtil.appendPercent(value);
    }

    public void readDEWPOINT(String value) {
        this.dewpoint = ValueDescriptionUtil.appendTemperature(value);
    }

    public String getTemperature() {
        return temperature;
    }

    public String getBattery() {
        return battery;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getDewpoint() {
        return dewpoint;
    }
}
