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

package li.klass.fhem.domain.setlist;

import static li.klass.fhem.util.ValueExtractUtil.extractLeadingFloat;

public class SetListSliderValue implements SetListValue {
    private final float start;
    private final float stop;
    private final float step;

    public SetListSliderValue(String[] parts) {
        start = extractLeadingFloat(parts[1]);
        step = extractLeadingFloat(parts[2]);
        stop = extractLeadingFloat(parts[3]);
    }

    public SetListSliderValue(float start, float step, float stop) {
        this.step = step;
        this.stop = stop;
        this.start = start;
    }

    public float getStart() {
        return start;
    }

    public float getStop() {
        return stop;
    }

    public float getStep() {
        return step;
    }

    @Override
    public String toString() {
        return "SetListSliderValue{" +
                "start=" + start +
                ", stop=" + stop +
                ", step=" + step +
                '}';
    }

    @Override
    public String asText() {
        return "slider," + start + "," + step + "," + stop;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SetListSliderValue that = (SetListSliderValue) o;

        if (start != that.start) return false;
        if (step != that.step) return false;
        if (stop != that.stop) return false;

        return true;
    }

    @Override
    public int hashCode() {
        float result = start;
        result = 31 * result + stop;
        result = 31 * result + step;
        return (int) (result * 100);
    }
}
