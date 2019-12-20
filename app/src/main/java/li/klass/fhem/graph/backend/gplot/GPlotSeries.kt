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
package li.klass.fhem.graph.backend.gplot

import li.klass.fhem.R
import java.io.Serializable

data class ViewSpec(val title: String,
                    val lineType: GPlotSeries.LineType,
                    val axis: GPlotSeries.Axis? = null, val color: GPlotSeries.SeriesColor? = null,
                    val seriesType: GPlotSeries.SeriesType = GPlotSeries.SeriesType.DEFAULT,
                    val lineWidth: Float = 1F) : Serializable


sealed class DataProviderSpec : Serializable {
    abstract val pattern: String

    data class FileLog(override val pattern: String) : DataProviderSpec()
    data class DbLog(override val pattern: String) : DataProviderSpec()
    data class CustomLogDevice(override val pattern: String, val logDevice: String) : DataProviderSpec()
}

data class GraphDataProvider(val fileLog: DataProviderSpec.FileLog? = null,
                             val dbLog: DataProviderSpec.DbLog? = null,
                             val customLogDevice: DataProviderSpec.CustomLogDevice? = null) : Serializable

data class GPlotSeries(val viewSpec: ViewSpec, val dataProvider: GraphDataProvider) : Serializable {
    enum class LineType {
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

    enum class Axis {
        LEFT,
        RIGHT
    }

    enum class SeriesType {
        DEFAULT,
        FILL,
        DOT
    }

    enum class SeriesColor(val colorAttribute: Int) {
        RED(R.attr.seriesColorRED),
        GREEN(R.attr.seriesColorGREEN),
        BLUE(R.attr.seriesColorBLUE),
        MAGENTA(R.attr.seriesColorMAGENTA),
        BROWN(R.attr.seriesColorBROWN),
        WHITE(R.attr.seriesColorWHITE),
        OLIVE(R.attr.seriesColorOLIVE),
        GRAY(R.attr.seriesColorGRAY),
        YELLOW(R.attr.seriesColorYELLOW);
    }
}