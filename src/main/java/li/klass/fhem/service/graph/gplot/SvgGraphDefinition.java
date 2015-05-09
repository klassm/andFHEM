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

package li.klass.fhem.service.graph.gplot;

import java.io.Serializable;

import li.klass.fhem.domain.log.LogDevice;

public class SvgGraphDefinition implements Serializable {
    private final String name;
    private final GPlotDefinition gPlotDefinition;
    private final LogDevice<?> logDevice;

    public SvgGraphDefinition(String name, GPlotDefinition gPlotDefinition, LogDevice<?> logDevice) {
        this.name = name;
        this.gPlotDefinition = gPlotDefinition;
        this.logDevice = logDevice;
    }

    public String getName() {
        return name;
    }

    public GPlotDefinition getPlotDefinition() {
        return gPlotDefinition;
    }

    public LogDevice<?> getLogDevice() {
        return logDevice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SvgGraphDefinition that = (SvgGraphDefinition) o;

        return !(name != null ? !name.equals(that.name) : that.name != null)
                && !(gPlotDefinition != null ? !gPlotDefinition.equals(that.gPlotDefinition) : that.gPlotDefinition != null)
                && !(logDevice != null ? !logDevice.equals(that.logDevice) : that.logDevice != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (gPlotDefinition != null ? gPlotDefinition.hashCode() : 0);
        result = 31 * result + (logDevice != null ? logDevice.hashCode() : 0);
        return result;
    }
}
