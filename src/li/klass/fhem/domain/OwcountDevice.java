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
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine1;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine2;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine3;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import org.w3c.dom.NamedNodeMap;

@FloorplanViewSettings(showState = true)
@SupportsWidget(MediumInformationWidgetView.class)
public class OwcountDevice extends Device<OwcountDevice> {

    @ShowField(description = R.string.counterA, showInOverview = true)
    @WidgetMediumLine1(description = R.string.counterA)
    private float counterA;
    @ShowField(description = R.string.counterB, showInOverview = true)
    @WidgetMediumLine2(description = R.string.counterB)
    private float counterB;
    private float correlationA;
    private float correlationB;
    @ShowField(description = R.string.present, showInOverview = true)
    private String present;
    @ShowField(description = R.string.warnings)
    @WidgetMediumLine3(description = R.string.warnings)
    private String warnings;

    
    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("COUNTERS.A")) {
            this.counterA = Float.valueOf(nodeContent);
        } else if (keyValue.equals("COUNTERS.B")) {
            this.counterB = Float.valueOf(nodeContent);
        } else if (keyValue.equals("CORR1")) {
            this.correlationA = Float.valueOf(nodeContent);
        } else if (keyValue.equals("CORR2")) {
            this.correlationB = Float.valueOf(nodeContent);
        } else if (keyValue.equals("PRESENT")) {
            Resources resources = AndFHEMApplication.getContext().getResources();
            this.present = nodeContent.equals("1") ? resources.getString(R.string.yes) : resources.getString(R.string.no);
        } else if (keyValue.equals("WARNINGS")) {
            this.warnings = nodeContent;
        }
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
}
