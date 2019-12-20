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

import com.crashlytics.android.Crashlytics
import com.google.common.base.Charsets
import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.google.common.base.Strings
import com.google.common.collect.*
import li.klass.fhem.graph.backend.gplot.GPlotSeries.*
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.JarURLConnection
import java.net.URL
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

class TemporaryGPlotSeries {
    var title = ""
    var logDef: String? = null
    var lineType = LineType.LINES
    var logDevice: String? = null
    var axis: Axis? = null
    var color: SeriesColor? = null
    var seriesType = SeriesType.DEFAULT
    var lineWidth = 1f
    fun toGPlotSeries(): GPlotSeries {
        return GPlotSeries(title, logDef, lineType, logDevice, axis, color, seriesType, lineWidth)
    }
}

@Singleton
class GPlotParser @Inject constructor() {
    private val TO_COLOR = ImmutableMap.builder<String, SeriesColor>().put("l0", SeriesColor.RED)
            .put("l1", SeriesColor.GREEN).put("l2", SeriesColor.BLUE).put("l3", SeriesColor.MAGENTA)
            .put("l4", SeriesColor.BROWN).put("l5", SeriesColor.WHITE).put("l6", SeriesColor.OLIVE)
            .put("l7", SeriesColor.GRAY).put("l8", SeriesColor.YELLOW).build()

    fun parseSafe(content: String): Optional<GPlotDefinition> {
        return try {
            Optional.of(parse(content))
        } catch (e: Exception) {
            LOGGER.warn("parseSafe() - cannot parse: \r\n$content", e)
            Crashlytics.setString("content", content)
            Crashlytics.logException(e)
            Optional.absent()
        }
    }

    fun parse(content: String): GPlotDefinition {
        val lines = content.split("[\r\n]".toRegex()).map { it.trim() }
        val setsDeclarations = extractSetsFrom(lines)
        val definition = GPlotDefinition()
        definition.leftAxis = createAxis(setsDeclarations, "y")
        definition.rightAxis = createAxis(setsDeclarations, "y2")
        val series = extractSeriesFrom(lines)
        for (s in series) {
            (if (s.axis === Axis.LEFT) definition.leftAxis else definition.rightAxis).addSeries(s)
        }
        return definition
    }

    private fun createAxis(setsDeclarations: Map<String, String?>, prefix: String): GPlotAxis {
        val labelKey = prefix + "label"
        val rightLabel =
                if (setsDeclarations.containsKey(labelKey)) setsDeclarations[labelKey] else ""
        val rangeKey = prefix + "range"
        var optRange = Optional.absent<Range<Double?>?>()
        val range = setsDeclarations[rangeKey]
        if (range != null) {
            val rangeValue =
                    range.replace("[\\[\\]]".toRegex(), "").replace("min", "").replace("max", "")
                            .trim { it <= ' ' }
            val parts = rangeValue.split(":").toTypedArray()
            optRange = calculateRange(rangeValue, parts)
        }
        return GPlotAxis(rightLabel, optRange)
    }

    private fun calculateRange(rangeValue: String,
                               parts: Array<String>): Optional<Range<Double?>?> {
        return if (Strings.isNullOrEmpty(rangeValue) || rangeValue == ":") {
            Optional.absent()
        } else if (rangeValue.startsWith(":")) {
            Optional.of(Range.atMost(parts[0].toDouble()))
        } else if (rangeValue.endsWith(":")) {
            Optional.of(Range.atLeast(parts[0].toDouble()))
        } else {
            Optional.of(Range.closed(parts[0].toDouble(), parts[1].toDouble()))
        }
    }

    private fun extractSeriesFrom(lines: List<String>): List<GPlotSeries> {
        val result: MutableList<GPlotSeries> = Lists.newArrayList()
        val temporarySeries: Queue<TemporaryGPlotSeries> = LinkedList()
        val colors: MutableList<SeriesColor> = SeriesColor.values().toMutableList()
        var plotFound = false
        for (line in lines) {
            if (line.startsWith("plot")) {
                plotFound = true
            }
            val spaceSeparatedParts = line.split(" ").toTypedArray()
            if (line.startsWith(
                            "#") && spaceSeparatedParts.size == 2 && !spaceSeparatedParts[0].matches(
                            "[#]+[ ]*".toRegex()) && spaceSeparatedParts[1].contains(":")) {
                val logDevice = spaceSeparatedParts[0].substring(1)
                val newSeries = TemporaryGPlotSeries()
                if (!logDevice.equals("FileLog", ignoreCase = true) && !logDevice.equals("DBLog",
                                                                                         ignoreCase = true)) {
                    newSeries.logDevice = logDevice
                }
                newSeries.logDef = spaceSeparatedParts[1]
                temporarySeries.add(newSeries)
            } else if (plotFound) {
                val series = temporarySeries.peek()
                if (series == null) {
                    LOGGER.error("extractSeriesFrom - builder is null")
                    break
                }
                var attributeFound = handleAxis(line, series)
                attributeFound = handleTitle(line, series) or attributeFound
                attributeFound = handleLineType(line, series) or attributeFound
                attributeFound = handleSeriesType(line, series, colors) or attributeFound
                attributeFound = handleLineWidth(line, series) or attributeFound
                LOGGER.trace("extractSeriesFrom - builder is $series")
                if (attributeFound) {
                    if (series.color == null) {
                        val color = Iterables.getFirst(colors, SeriesColor.RED)
                        series.color = color
                        colors.remove(color)
                    }
                    result.add(series.toGPlotSeries())
                    temporarySeries.remove()
                }
            }
        }
        return result
    }

    private fun handleLineWidth(line: String, series: TemporaryGPlotSeries): Boolean {
        val matcher = LINE_WIDTH_PATTERN.matcher(line)
        if (matcher.find()) {
            val lineWidth = matcher.group(1)!!.toFloat()
            series.lineWidth = lineWidth
            return true
        }
        return false
    }

    private fun handleSeriesType(line: String, builder: TemporaryGPlotSeries,
                                 colors: MutableList<SeriesColor>): Boolean {
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
            val color = TO_COLOR[colorDesc]
            colors.remove(color)
            builder.color = color
            builder.seriesType = seriesType
            return true
        }
        return false
    }

    private fun handleLineType(line: String, builder: TemporaryGPlotSeries): Boolean {
        val typeMatcher = TYPE_PATTERN.matcher(line)
        if (typeMatcher.find()) {
            try {
                builder.lineType =
                        LineType.valueOf(typeMatcher.group(1)!!.toUpperCase(Locale.getDefault()))
                return true
            } catch (e: IllegalArgumentException) {
                LOGGER.debug("cannot find type for {}", typeMatcher.group(1))
            }
        }
        return false
    }

    private fun handleTitle(line: String, builder: TemporaryGPlotSeries): Boolean {
        val titleMatcher = TITLE_PATTERN.matcher(line)
        if (titleMatcher.find()) {
            builder.title = titleMatcher.group(1)!!
            return true
        }
        return false
    }

    private fun handleAxis(line: String, builder: TemporaryGPlotSeries): Boolean {
        val axesMatcher = AXIS_PATTERN.matcher(line)
        return if (axesMatcher.find()) {
            val axis = axesMatcher.group(1)
            when (axis) {
                "1" -> builder.axis = Axis.LEFT
                "2" -> builder.axis = Axis.RIGHT
            }
            true
        } else {
            builder.axis = Axis.LEFT
            false
        }
    }

    private fun extractSetsFrom(lines: List<String>): Map<String, String?> {
        val out: MutableMap<String, String?> = Maps.newHashMap()
        for (line in lines) {
            val matcher = SETS_PATTERN.matcher(line)
            if (!matcher.matches()) {
                continue
            }
            out[matcher.group(1)!!] = matcher.group(2)
        }
        return out
    }

    val defaultGPlotFiles: Map<String, GPlotDefinition>
        get() {
            try {
                val url = GPlotParser::class.java.getResource("dummy.txt")
                val scheme = url!!.protocol
                Preconditions.checkArgument(scheme == "jar")
                return readDefinitionsFromJar(url)
            } catch (e: Exception) {
                LOGGER.error("loadDefaultGPlotFiles() - cannot load default files", e)
            }
            return emptyMap()
        }

    @Throws(IOException::class)
    private fun readDefinitionsFromJar(url: URL?): Map<String, GPlotDefinition> {
        val result: MutableMap<String, GPlotDefinition> = Maps.newHashMap()
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
    }
}