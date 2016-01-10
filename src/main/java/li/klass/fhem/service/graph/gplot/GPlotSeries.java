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

import com.google.common.base.Optional;

import java.io.Serializable;

import static li.klass.fhem.service.graph.gplot.GPlotSeries.SeriesType.DEFAULT;

public class GPlotSeries implements Serializable {
    private String title;
    private String logDef;
    private LineType lineType;
    private Axis axis;
    private SeriesColor color;
    private SeriesType seriesType;
    private final float lineWidth;

    private GPlotSeries(Builder builder) {
        title = builder.title;
        logDef = builder.logDef;
        lineType = builder.lineType;
        axis = builder.axis;
        color = builder.color.get();
        seriesType = builder.seriesType;
        lineWidth = builder.lineWidth;
    }

    public String getTitle() {
        return title;
    }

    public String getLogDef() {
        return logDef;
    }

    public LineType getLineType() {
        return lineType;
    }

    public Axis getAxis() {
        return axis;
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
                !(logDef != null ? !logDef.equals(that.logDef) : that.logDef != null)
                && lineType == that.lineType
                && axis == that.axis
                && color == that.color
                && seriesType == that.seriesType
                && lineWidth == that.lineWidth;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (logDef != null ? logDef.hashCode() : 0);
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
                ", logDef='" + logDef + '\'' +
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

    public enum Axis {
        LEFT, RIGHT
    }

    public enum SeriesType {
        DEFAULT,
        FILL,
        DOT
    }

    public enum SeriesColor {
        RED(Color.RED),
        GREEN(0xFF004700),
        BLUE(Color.BLUE),
        MAGENTA(Color.MAGENTA),
        BROWN(0xA52A2A),
        WHITE(Color.BLACK),
        OLIVE(0x808000),
        GRAY(0x5A5A5A),
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
        private String logDef;
        private LineType lineType = LineType.LINES;
        private Axis axis;
        private Optional<SeriesColor> color = Optional.absent();
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

        public Builder withLogDef(final String fileLogDef) {
            this.logDef = fileLogDef;
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
                this.color = Optional.of(color);
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

        public boolean isColorSet() {
            return this.color.isPresent();
        }

        public GPlotSeries build() {
            return new GPlotSeries(this);
        }

        @Override
        public String toString() {
            return "GPlotSeries.Builder{" +
                    "title='" + title + '\'' +
                    ", logDef='" + logDef + '\'' +
                    ", lineType=" + lineType +
                    ", axis=" + axis +
                    ", color=" + color +
                    ", seriesType=" + seriesType +
                    ", lineWidth=" + lineWidth +
                    '}';
        }
    }
}
