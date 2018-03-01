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

package li.klass.fhem.domain.setlist.typeEntry;

import li.klass.fhem.domain.setlist.SetListItem;
import li.klass.fhem.domain.setlist.SetListItemType;

import static li.klass.fhem.util.ValueExtractUtil.extractLeadingFloat;

public class SliderSetListEntry extends SetListItem {
    private final float start;
    private final float stop;
    private final float step;

    public SliderSetListEntry(String key, String[] parts) {
        this(key, extractLeadingFloat(parts[1]), extractLeadingFloat(parts[2]), extractLeadingFloat(parts[3]));
    }

    public SliderSetListEntry(String key, float start, float step, float stop) {
        super(key, SetListItemType.SLIDER);

        this.step = step;
        this.stop = stop;
        this.start = start;
    }

    public double getStart() {
        return start;
    }

    public double getStop() {
        return stop;
    }

    public double getStep() {
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

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SliderSetListEntry that = (SliderSetListEntry) o;

        if (Float.compare(that.start, start) != 0) return false;
        if (Float.compare(that.stop, stop) != 0) return false;
        return Float.compare(that.step, step) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (start != +0.0f ? Float.floatToIntBits(start) : 0);
        result = 31 * result + (stop != +0.0f ? Float.floatToIntBits(stop) : 0);
        result = 31 * result + (step != +0.0f ? Float.floatToIntBits(step) : 0);
        return result;
    }
}
