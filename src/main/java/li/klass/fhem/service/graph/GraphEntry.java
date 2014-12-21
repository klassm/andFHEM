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

package li.klass.fhem.service.graph;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

/**
 * Transfer object representing one dot within a future graph.
 */
public class GraphEntry implements Comparable<GraphEntry> {
    private float value;
    private DateTime date;

    public GraphEntry(DateTime date, float value) {
        this.value = value;
        this.date = date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public float getValue() {
        return value;
    }

    public DateTime getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "GraphEntry{" +
                "value=" + value +
                ", date=" + date +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphEntry that = (GraphEntry) o;

        return Float.compare(that.value, value) == 0 && !(date != null ? !date.equals(that.date) : that.date != null);

    }

    @Override
    public int hashCode() {
        int result = (value != +0.0f ? Float.floatToIntBits(value) : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(@NotNull GraphEntry graphEntry) {
        return (date).compareTo(graphEntry.getDate());
    }


}
