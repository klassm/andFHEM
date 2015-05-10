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

import android.graphics.Color;

import java.io.Serializable;

import static li.klass.fhem.service.graph.gplot.GPlotSeries.SeriesType.DEFAULT;

public class GPlotSeries implements Serializable {
    private String title;
    private String fileLogDef;
    private String dbLogDef;
    private LineType lineType;
    private Axis axis;
    private SeriesColor color;
    private SeriesType seriesType;
    private final float lineWidth;

    private GPlotSeries(Builder builder) {
        title = builder.title;
        fileLogDef = builder.fileLogDef;
        dbLogDef = builder.dbLogDef;
        lineType = builder.lineType;
        axis = builder.axis;
        color = builder.color;
        seriesType = builder.seriesType;
        lineWidth = builder.lineWidth;
    }

    public String getTitle() {
        return title;
    }

    public String getFileLogDef() {
        return fileLogDef;
    }

    public LineType getLineType() {
        return lineType;
    }

    public Axis getAxis() {
        return axis;
    }

    public String getDbLogDef() {
        return dbLogDef;
    }

    public SeriesColor getColor() {
        return color;
    }

    public SeriesType getSeriesType() {
        return seriesType;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GPlotSeries that = (GPlotSeries) o;

        return !(title != null ? !title.equals(that.title) : that.title != null) &&
                !(fileLogDef != null ? !fileLogDef.equals(that.fileLogDef) : that.fileLogDef != null)
                && !(dbLogDef != null ? !dbLogDef.equals(that.dbLogDef) : that.dbLogDef != null)
                && lineType == that.lineType
                && axis == that.axis
                && color == that.color
                && seriesType == that.seriesType
                && lineWidth == that.lineWidth;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (fileLogDef != null ? fileLogDef.hashCode() : 0);
        result = 31 * result + (dbLogDef != null ? dbLogDef.hashCode() : 0);
        result = 31 * result + (lineType != null ? lineType.hashCode() : 0);
        result = 31 * result + (axis != null ? axis.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (seriesType != null ? seriesType.hashCode() : 0);
        result = 31 * result + (lineWidth != +0.0f ? Float.floatToIntBits(lineWidth) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GPlotSeries{" +
                "title='" + title + '\'' +
                ", fileLogDef='" + fileLogDef + '\'' +
                ", dbLogDef='" + dbLogDef + '\'' +
                ", lineType=" + lineType +
                ", axis=" + axis +
                ", color=" + color +
                ", seriesType=" + seriesType +
                ", lineWidth=" + lineWidth +
                '}';
    }

    public enum LineType {
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

    public enum SeriesType {
        DEFAULT,
        FILL,
        DOT
    }

    public enum SeriesColor {
        RED(Color.RED),
        GREEN(Color.GREEN),
        BLUE(Color.BLUE),
        MAGENTA(Color.MAGENTA),
        BROWN(0xA52A2A),
        WHITE(Color.WHITE),
        OLIVE(0x808000),
        GRAY(Color.GRAY),
        YELLOW(Color.YELLOW),;

        private final int color;

        SeriesColor(int color) {
            this.color = color;
        }

        public int getHexColor() {
            return color;
        }
    }

    public static final class Builder {
        private String title = "";
        private String fileLogDef;
        private String dbLogDef;
        private LineType lineType = LineType.LINES;
        public Axis axis;
        private SeriesColor color = SeriesColor.RED;
        private SeriesType seriesType = DEFAULT;
        private float lineWidth = 1;

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

        public Builder withLineType(final LineType lineType) {
            this.lineType = lineType;
            return this;
        }

        public Builder withAxis(final Axis axis) {
            this.axis = axis;
            return this;
        }

        public Builder withColor(final SeriesColor color) {
            if (color != null) {
                this.color = color;
            }
            return this;
        }

        public Builder withSeriesType(final SeriesType seriesType) {
            this.seriesType = seriesType;
            return this;
        }

        public Builder withLineWith(float lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        public GPlotSeries build() {
            return new GPlotSeries(this);
        }
    }
}
