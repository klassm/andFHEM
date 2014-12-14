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

import android.content.res.Resources;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine1;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine2;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine3;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.ShowField;

@SupportsWidget(MediumInformationWidgetView.class)
@SuppressWarnings("unused")
public class OwcountDevice extends Device<OwcountDevice> {

    @ShowField(description = ResourceIdMapper.counterA, showInOverview = true)
    @WidgetMediumLine1(description = ResourceIdMapper.counterA)
    private float counterA;
    @ShowField(description = ResourceIdMapper.counterB, showInOverview = true)
    @WidgetMediumLine2(description = ResourceIdMapper.counterB)
    private float counterB;
    private float correlationA;
    private float correlationB;
    @ShowField(description = ResourceIdMapper.present, showInOverview = true)
    private String present;
    @ShowField(description = ResourceIdMapper.warnings)
    @WidgetMediumLine3(description = ResourceIdMapper.warnings)
    private String warnings;

    public void readCOUNTERS_A(String value) {
        this.counterA = Float.valueOf(value);
    }

    public void readCOUNTERS_B(String value) {
        this.counterB = Float.valueOf(value);
    }

    public void readCORR1(String value) {
        this.correlationA = Float.valueOf(value);
    }

    public void readCORR2(String value) {
        this.correlationB = Float.valueOf(value);
    }

    public void readPRESENT(String value) {
        Resources resources = AndFHEMApplication.getContext().getResources();
        this.present = value.equals("1") ? resources.getString(R.string.yes) : resources.getString(R.string.no);
    }

    public void readWARNINGS(String value) {
        this.warnings = value;
    }

    public float getCounterA() {
        return counterA;
    }

    public float getCounterB() {
        return counterB;
    }

    public float getCorrelationA() {
        return correlationA;
    }

    public float getCorrelationB() {
        return correlationB;
    }

    public String getPresent() {
        return present;
    }

    public String getWarnings() {
        return warnings;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.USAGE;
    }

    @Override
    public boolean isSensorDevice() {
        return true;
    }

    @Override
    public long getTimeRequiredForStateError() {
        return OUTDATED_DATA_MS_DEFAULT;
    }
}
