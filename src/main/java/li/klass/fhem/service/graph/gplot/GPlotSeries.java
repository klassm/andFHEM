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

public class GPlotSeries implements Serializable {
    private String title;
    private String fileLogDef;
    private String dbLogDef;
    private Type type;
    private Axis axis;

    private GPlotSeries(Builder builder) {
        title = builder.title;
        fileLogDef = builder.fileLogDef;
        dbLogDef = builder.dbLogDef;
        type = builder.type;
        axis = builder.axis;
    }

    public String getTitle() {
        return title;
    }

    public String getFileLogDef() {
        return fileLogDef;
    }

    public Type getType() {
        return type;
    }

    public Axis getAxis() {
        return axis;
    }

    public String getDbLogDef() {
        return dbLogDef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GPlotSeries that = (GPlotSeries) o;

        return !(title != null ? !title.equals(that.title) : that.title != null) &&
                !(fileLogDef != null ? !fileLogDef.equals(that.fileLogDef) : that.fileLogDef != null)
                && !(dbLogDef != null ? !dbLogDef.equals(that.dbLogDef) : that.dbLogDef != null)
                && type == that.type && axis == that.axis;

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (fileLogDef != null ? fileLogDef.hashCode() : 0);
        result = 31 * result + (dbLogDef != null ? dbLogDef.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (axis != null ? axis.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GPlotSeries{" +
                "title='" + title + '\'' +
                ", fileLogDef='" + fileLogDef + '\'' +
                ", dbLogDef='" + dbLogDef + '\'' +
                ", type=" + type +
                ", axis=" + axis +
                '}';
    }

    public enum Type {
        LINES,
        POINTS,
        STEPS,
        FSTEPS,
        HISTEPS,
        BARS,
        CUBIC,
        QUADRATIC,
        QUADRATICSMOOTH
    }

    enum Axis {
        LEFT, RIGHT
    }


    public static final class Builder {
        private String title = "";
        private String fileLogDef;
        private String dbLogDef;
        private Type type = Type.LINES;
        public Axis axis;

        public Builder() {
        }

        public Builder withTitle(final String title) {
            if (title != null) {
                this.title = title;
            }
            return this;
        }

        public Builder withFileLogDef(final String fileLogDef) {
            this.fileLogDef = fileLogDef;
            return this;
        }

        public Builder withDbLogDef(final String dbLogDef) {
            this.dbLogDef = dbLogDef;
            return this;
        }

        public Builder withType(final Type type) {
            this.type = type;
            return this;
        }

        public Builder withAxis(final Axis axis) {
            this.axis = axis;
            return this;
        }

        public GPlotSeries build() {
            return new GPlotSeries(this);
        }
    }
}
