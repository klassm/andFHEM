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

package li.klass.fhem.domain.genericview;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import li.klass.fhem.resources.ResourceIdMapper;

@SuppressWarnings("ClassExplicitlyAnnotation")
public class OverviewViewSettingsCache implements OverviewViewSettings, Serializable {

    private final boolean showState;
    private final boolean showMeasured;
    private final ResourceIdMapper stateStringId;
    private final ResourceIdMapper measuredStringId;

    public OverviewViewSettingsCache(OverviewViewSettings settings) {
        showState = settings.showState();
        showMeasured = settings.showMeasured();
        stateStringId = settings.stateStringId();
        measuredStringId = settings.measuredStringId();
    }

    public OverviewViewSettingsCache(boolean showState, boolean showMeasured, ResourceIdMapper stateStringId, ResourceIdMapper measuredStringId) {
        this.showState = showState;
        this.showMeasured = showMeasured;
        this.stateStringId = stateStringId;
        this.measuredStringId = measuredStringId;
    }

    public OverviewViewSettingsCache(boolean showState, boolean showMeasured) {
        this(showState, showMeasured, null, null);
    }

    public boolean showState() {
        return showState;
    }

    public boolean showMeasured() {
        return showMeasured;
    }

    public ResourceIdMapper stateStringId() {
        return stateStringId;
    }


    public ResourceIdMapper measuredStringId() {
        return measuredStringId;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return OverviewViewSettings.class;
    }

}
