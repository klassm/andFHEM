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

package li.klass.fhem.graph.backend.gplot;

import java.io.Serializable;

public class GPlotDefinition implements Serializable {

    private GPlotAxis leftAxis;
    private GPlotAxis rightAxis;

    public GPlotAxis getLeftAxis() {
        return leftAxis;
    }

    public GPlotAxis getRightAxis() {
        return rightAxis;
    }

    public void setLeftAxis(GPlotAxis leftAxis) {
        this.leftAxis = leftAxis;
    }

    public void setRightAxis(GPlotAxis rightAxis) {
        this.rightAxis = rightAxis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GPlotDefinition that = (GPlotDefinition) o;

        return !(leftAxis != null ? !leftAxis.equals(that.leftAxis) : that.leftAxis != null)
                && !(rightAxis != null ? !rightAxis.equals(that.rightAxis) : that.rightAxis != null);

    }

    @Override
    public int hashCode() {
        int result = leftAxis != null ? leftAxis.hashCode() : 0;
        result = 31 * result + (rightAxis != null ? rightAxis.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GPlotDefinition{" +
                "leftAxis=" + leftAxis +
                ", rightAxis=" + rightAxis +
                '}';
    }
}
