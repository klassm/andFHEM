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

import com.google.common.base.Optional;
import com.google.common.collect.Range;

import java.io.Serializable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class GPlotAxis implements Serializable {
    private String label;
    private List<GPlotSeries> series = newArrayList();

    private Optional<Range<Double>> range = Optional.absent();

    public GPlotAxis(String label, Optional<Range<Double>> range) {
        this.label = label;
        this.range = range;
    }

    public String getLabel() {
        return label;
    }

    public List<GPlotSeries> getSeries() {
        return series;
    }

    public void addSeries(GPlotSeries s) {
        series.add(s);
    }

    public Optional<Range<Double>> getRange() {
        return range;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GPlotAxis gPlotAxis = (GPlotAxis) o;

        return !(label != null ? !label.equals(gPlotAxis.label) : gPlotAxis.label != null)
                && !(series != null ? !series.equals(gPlotAxis.series) : gPlotAxis.series != null)
                && !(range != null ? !range.equals(gPlotAxis.range) : gPlotAxis.range != null);

    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (series != null ? series.hashCode() : 0);
        result = 31 * result + (range != null ? range.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GPlotAxis{" +
                "label='" + label + '\'' +
                ", series=" + series +
                ", range=" + range +
                '}';
    }
}
