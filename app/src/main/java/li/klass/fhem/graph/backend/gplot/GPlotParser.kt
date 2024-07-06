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

import li.klass.fhem.graph.backend.gplot.GPlotSeries.*
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.JarURLConnection
import java.net.URL
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GPlotParser @Inject constructor() {
    fun parseSafe(content: String): GPlotDefinition? {
        return try {
            parse(content)
        } catch (e: Exception) {
            LOGGER.warn("parseSafe() - cannot parse: \r\n$content", e)
            null
        }
    }

    fun parse(content: String): GPlotDefinition {
        val lines = content
                .split("[\r\n]".toRegex()).map { it.trim() }
        val setsDeclarations = extractSetsFrom(lines)
        val series = extractGPlotSeries(lines)
        val leftAxisSeries = series.filter { it.viewSpec.axis == Axis.LEFT }
        val rightAxisSeries = series.filter { it.viewSpec.axis == Axis.RIGHT }

        return GPlotDefinition(
                leftAxis = createAxis(setsDeclarations, "y", leftAxisSeries),
                rightAxis = createAxis(setsDeclarations, "y2", rightAxisSeries)
        )
    }

    private fun createAxis(setsDeclarations: Map<String, String?>, prefix: String, series: List<GPlotSeries>): GPlotAxis {
        val labelKey = prefix + "label"
        val label = setsDeclarations[labelKey] ?: ""
        val rangeKey = prefix + "range"
        val range = setsDeclarations[rangeKey]?.let {
            val rangeValue =
                    it.replace("[\\[\\]]".toRegex(), "")
                            .replace("min", "")
                            .replace("max", "")
                            .trim()
            val parts = rangeValue.split(":").toTypedArray()
            calculateRange(rangeValue, parts)
        }
        return GPlotAxis(label, range, series)
    }

    private fun calculateRange(rangeValue: String,
                               parts: Array<String>): Range? {
        return if (rangeValue.isEmpty() || rangeValue == ":") {
            null
        } else if (rangeValue.startsWith(":")) {
            Range.atMost(parts[0].toFloat())
        } else if (rangeValue.endsWith(":")) {
            Range.atLeast(parts[0].toFloat())
        } else {
            Range.closed(parts[0].toFloat(), parts[1].toFloat())
        }
    }

    private fun extractGPlotSeries(lines: List<String>): List<GPlotSeries> {
        val viewSpecs = extractViewSpecs(lines)
        val dataProviderSpecs = extractDataProviderSpecs(lines)
        val customProviders = dataProviderSpecs.filterIsInstance<DataProviderSpec.CustomLogDevice>()
        val fileLogProviders = dataProviderSpecs.filterIsInstance<DataProviderSpec.FileLog>()
        val dbLogProviders = dataProviderSpecs.filterIsInstance<DataProviderSpec.DbLog>()

        return viewSpecs.mapIndexed { index, viewSpec ->
            val custom = customProviders.getOrNull(index)
            val fileLog = fileLogProviders.getOrNull(index)
            val dbLog = dbLogProviders.getOrNull(index)

            GPlotSeries(viewSpec = viewSpec, dataProvider = GraphDataProvider(
                    fileLog, dbLog, custom
            ))
        }
    }

    private fun extractViewSpecs(lines: List<String>): List<ViewSpec> {
        val plotLineIndex = lines.indexOfFirst { it.startsWith("plot ") }
        val colors = SeriesColor.values().toMutableList()
        val plotLines = lines.filterIndexed { index, _ -> index >= plotLineIndex }
                .filter { it.contains("title ") }

        return plotLines.mapNotNull { extractViewSpecFrom(it, colors) }
    }

    private fun extractViewSpecFrom(line: String, colors: MutableList<SeriesColor>): ViewSpec? {
        val (seriesType, color) = handleSeriesType(line, colors)
        return ViewSpec(
                title = handleTitle(line) ?: "",
                color = color,
                axis = handleAxis(line),
                seriesType = seriesType,
                lineType = handleLineType(line) ?: LineType.LINES,
                lineWidth = handleLineWidth(line) ?: 1f
        )
    }

    private fun extractDataProviderSpecs(
            lines: List<String>) = lines.asSequence()
            .filter { it.startsWith("#") }
            .map { it.split(" ") }
            .filter { it.size == 2 }
            .filterNot { it[0].matches("[#]+[ ]*".toRegex()) }
            .filter { it[1].contains(":") }
            .map { extractDataProviderSpecFrom(it) }
            .toList()

    private fun extractDataProviderSpecFrom(spaceSeparatedParts: List<String>): DataProviderSpec {
        val pattern = spaceSeparatedParts[1]
        return when (spaceSeparatedParts[0]) {
            "#FileLog" -> DataProviderSpec.FileLog(pattern)
            "#DbLog" -> DataProviderSpec.DbLog(pattern)
            else -> DataProviderSpec.CustomLogDevice(pattern, logDevice = spaceSeparatedParts[0].substring(1))
        }
    }

    private fun handleLineWidth(line: String): Float? {
        val matcher = LINE_WIDTH_PATTERN.matcher(line)
        if (matcher.find()) {
            return matcher.group(1)!!.toFloat()
        }
        return null
    }

    private fun handleSeriesType(line: String,
                                 colors: MutableList<SeriesColor>): Pair<SeriesType, SeriesColor> {
        val matcher = SERIES_TYPE_PATTERN.matcher(line)
        if (matcher.find()) {
            val colorDesc = matcher.group(1)!!
            val fillDesc = matcher.group(2)!!
            var seriesType = SeriesType.DEFAULT
            if (fillDesc.contains("fill")) {
                seriesType = SeriesType.FILL
            } else if (fillDesc.contains("dot")) {
                seriesType = SeriesType.DOT
            }
            val color = COLOR_MAPPING[colorDesc] ?: colors.getOrElse(0) { SeriesColor.RED }
            colors.remove(color)
            return seriesType to color
        }

        val color = colors.getOrElse(0) { SeriesColor.RED }
        colors.remove(color)
        return SeriesType.DEFAULT to color
    }

    private fun handleLineType(line: String): LineType? {
        val typeMatcher = TYPE_PATTERN.matcher(line)
        if (typeMatcher.find()) {
            try {
                return LineType.valueOf(typeMatcher.group(1)!!.toUpperCase(Locale.getDefault()))
            } catch (e: IllegalArgumentException) {
                LOGGER.debug("cannot find type for {}", typeMatcher.group(1))
            }
        }
        return null
    }

    private fun handleTitle(line: String): String? {
        val titleMatcher = TITLE_PATTERN.matcher(line)
        if (titleMatcher.find()) {
            return titleMatcher.group(1)!!
        }
        return null
    }

    private fun handleAxis(line: String): Axis {
        val axesMatcher = AXIS_PATTERN.matcher(line)
        return if (axesMatcher.find()) {
            return when (axesMatcher.group(1)) {
                "2" -> Axis.RIGHT
                else -> Axis.LEFT // "1"
            }
        } else {
            Axis.LEFT
        }
    }

    private fun extractSetsFrom(lines: List<String>): Map<String, String?> {
        val out: MutableMap<String, String?> = mutableMapOf()
        for (line in lines) {
            val matcher = SETS_PATTERN.matcher(line)
            if (!matcher.matches()) {
                continue
            }
            out[matcher.group(1)!!] = matcher.group(2)
        }
        return out
    }

    val defaultGPlotFiles: Map<String, GPlotDefinition> by lazy {
            try {
                val url = GPlotParser::class.java.getResource("dummy.txt")
                url!!.protocol
                readDefinitionsFromJar(url)
            } catch (e: Exception) {
                LOGGER.error("loadDefaultGPlotFiles() - cannot load default files", e)
                emptyMap<String, GPlotDefinition>()
            }
    }

    @Throws(IOException::class)
    private fun readDefinitionsFromJar(url: URL?): Map<String, GPlotDefinition> {
        val result: MutableMap<String, GPlotDefinition> = mutableMapOf()
        val con = url!!.openConnection() as JarURLConnection
        val archive = con.jarFile
        val entries = archive.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            if (entry.name.endsWith(".gplot")) {
                val filename = entry.name.substring(entry.name.lastIndexOf("/") + 1)
                val plotName = filename.substring(0, filename.indexOf("."))
                val resource = GPlotParser::class.java.getResourceAsStream(filename)
                        .use { it?.readBytes()?.toString(Charsets.UTF_8) ?: "" }
                result[plotName] = parse(resource)
            }
        }
        return result
    }

    companion object {
        private val SETS_PATTERN =
                Pattern.compile("set ([a-zA-Z0-9]+) [\"'\\[]?([^\"^']+)[\"'\\]]?")
        private val AXIS_PATTERN = Pattern.compile("axes x1y([12])")
        private val TITLE_PATTERN = Pattern.compile("title '([^']*)'")
        private val TYPE_PATTERN = Pattern.compile("with ([a-zA-Z]+)")
        private val SERIES_TYPE_PATTERN = Pattern.compile("(l[0-9])((dot|fill(_stripe|_gyr)?)?)")
        private val LINE_WIDTH_PATTERN = Pattern.compile("lw ([0-9]+(\\.[0-9]+)?)")
        private val LOGGER = LoggerFactory.getLogger(GPlotParser::class.java)
        private val COLOR_MAPPING = mapOf(
                "l0" to SeriesColor.RED,
                "l1" to SeriesColor.GREEN,
                "l2" to SeriesColor.BLUE,
                "l3" to SeriesColor.MAGENTA,
                "l4" to SeriesColor.BROWN,
                "l5" to SeriesColor.WHITE,
                "l6" to SeriesColor.OLIVE,
                "l7" to SeriesColor.GRAY,
                "l8" to SeriesColor.YELLOW
        )
    }
}