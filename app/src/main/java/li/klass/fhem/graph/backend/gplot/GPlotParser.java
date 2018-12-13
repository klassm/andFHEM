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

package li.klass.fhem.graph.backend.gplot;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.io.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import li.klass.fhem.graph.backend.gplot.GPlotSeries.SeriesColor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyMap;

@Singleton
public class GPlotParser {

    private static final Pattern SETS_PATTERN = Pattern.compile("set ([a-zA-Z0-9]+) [\"'\\[]?([^\"^']+)[\"'\\]]?");
    private static final Pattern AXIS_PATTERN = Pattern.compile("axes x1y([12])");
    private static final Pattern TITLE_PATTERN = Pattern.compile("title '([^']*)'");
    private static final Pattern TYPE_PATTERN = Pattern.compile("with ([a-zA-Z]+)");
    private static final Pattern SERIES_TYPE_PATTERN = Pattern.compile("(l[0-9])((dot|fill(_stripe|_gyr)?)?)");
    private static final Pattern LINE_WIDTH_PATTERN = Pattern.compile("lw ([0-9]+(\\.[0-9]+)?)");

    private static final Logger LOGGER = LoggerFactory.getLogger(GPlotParser.class);

    private ImmutableMap<String, SeriesColor> TO_COLOR = ImmutableMap.<String, SeriesColor>builder()
            .put("l0", SeriesColor.RED)
            .put("l1", SeriesColor.GREEN)
            .put("l2", SeriesColor.BLUE)
            .put("l3", SeriesColor.MAGENTA)
            .put("l4", SeriesColor.BROWN)
            .put("l5", SeriesColor.WHITE)
            .put("l6", SeriesColor.OLIVE)
            .put("l7", SeriesColor.GRAY)
            .put("l8", SeriesColor.YELLOW)
            .build();

    @Inject
    public GPlotParser() {
    }

    public Optional<GPlotDefinition> parseSafe(String content) {
        try {
            return Optional.of(parse(content));
        } catch (Exception e) {
            LOGGER.warn("parseSafe() - cannot parse: \r\n" + content, e);
            Crashlytics.setString("content", content);
            Crashlytics.logException(e);
            return Optional.absent();
        }
    }

    public GPlotDefinition parse(String content) {
        List<String> lines = newArrayList(content.split("[\\r\\n]"));
        Map<String, String> setsDeclarations = extractSetsFrom(lines);

        GPlotDefinition definition = new GPlotDefinition();

        definition.setLeftAxis(createAxis(setsDeclarations, "y"));
        definition.setRightAxis(createAxis(setsDeclarations, "y2"));

        List<GPlotSeries> series = extractSeriesFrom(lines);
        for (GPlotSeries s : series) {
            (s.getAxis() == GPlotSeries.Axis.LEFT ? definition.getLeftAxis() : definition.getRightAxis())
                    .addSeries(s);
        }

        return definition;
    }

    private GPlotAxis createAxis(Map<String, String> setsDeclarations, String prefix) {
        String labelKey = prefix + "label";
        String rightLabel = setsDeclarations.containsKey(labelKey) ?
                setsDeclarations.get(labelKey) : "";

        String rangeKey = prefix + "range";
        Optional<Range<Double>> optRange = Optional.absent();
        if (setsDeclarations.containsKey(rangeKey)) {
            String rangeValue = setsDeclarations.get(rangeKey).replaceAll("[\\[\\]]", "")
                    .replace("min", "")
                    .replace("max", "")
                    .trim();
            String[] parts = rangeValue.split(":");

            optRange = calculateRange(rangeValue, parts);
        }

        return new GPlotAxis(rightLabel, optRange);
    }

    @NonNull
    private Optional<Range<Double>> calculateRange(String rangeValue, String[] parts) {
        if (Strings.isNullOrEmpty(rangeValue) || rangeValue.equals(":")) {
            return Optional.absent();
        } else if (rangeValue.startsWith(":")) {
            return Optional.of(Range.atMost(Double.parseDouble(parts[0])));
        } else if (rangeValue.endsWith(":")) {
            return Optional.of(Range.atLeast(Double.parseDouble(parts[0])));
        } else {
            return Optional.of(Range.closed(Double.parseDouble(parts[0]), Double.parseDouble(parts[1])));
        }
    }

    private List<GPlotSeries> extractSeriesFrom(List<String> lines) {
        List<GPlotSeries> result = newArrayList();
        Queue<GPlotSeries.Builder> builders = new LinkedList<>();

        List<SeriesColor> colors = new ArrayList<>(Arrays.asList(SeriesColor.values()));
        boolean plotFound = false;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("plot")) {
                plotFound = true;
            }
            String[] spaceSeparatedParts = line.split(" ");
            if (line.startsWith("#")
                    && spaceSeparatedParts.length == 2
                    && !spaceSeparatedParts[0].matches("[#]+[ ]*")
                    && spaceSeparatedParts[1].contains(":")) {
                builders.add(new GPlotSeries.Builder().withLogDef(spaceSeparatedParts[1]));
            } else if (plotFound) {
                GPlotSeries.Builder builder = builders.peek();
                if (builder == null) {
                    LOGGER.error("extractSeriesFrom - builder is null");
                    break;
                }

                boolean attributeFound = handleAxis(line, builder);
                attributeFound = handleTitle(line, builder) | attributeFound;
                attributeFound = handleLineType(line, builder) | attributeFound;
                attributeFound = handleSeriesType(line, builder, colors) | attributeFound;
                attributeFound = handleLineWidth(line, builder) | attributeFound;

                LOGGER.trace("extractSeriesFrom - builder is " + builder);
                if (attributeFound) {
                    if (!builder.isColorSet()) {
                        SeriesColor color = Iterables.getFirst(colors, SeriesColor.RED);
                        builder.withColor(color);
                        colors.remove(color);
                    }
                    result.add(builder.build());
                    builders.remove();
                }
            }
        }

        return result;
    }

    private boolean handleLineWidth(String line, GPlotSeries.Builder builder) {
        Matcher matcher = LINE_WIDTH_PATTERN.matcher(line);
        if (matcher.find()) {
            float lineWidth = Float.parseFloat(matcher.group(1));
            builder.withLineWith(lineWidth);
            return true;
        }
        return false;
    }

    private boolean handleSeriesType(String line, GPlotSeries.Builder builder, List<SeriesColor> colors) {
        Matcher matcher = SERIES_TYPE_PATTERN.matcher(line);
        if (matcher.find()) {
            String colorDesc = matcher.group(1);
            String fillDesc = matcher.group(2);

            GPlotSeries.SeriesType seriesType = GPlotSeries.SeriesType.DEFAULT;
            if (fillDesc.contains("fill")) {
                seriesType = GPlotSeries.SeriesType.FILL;
            } else if (fillDesc.contains("dot")) {
                seriesType = GPlotSeries.SeriesType.DOT;
            }

            SeriesColor color = TO_COLOR.get(colorDesc);
            colors.remove(color);

            builder.withColor(color);
            builder.withSeriesType(seriesType);

            return true;
        }

        return false;
    }

    private boolean handleLineType(String line, GPlotSeries.Builder builder) {
        Matcher typeMatcher = TYPE_PATTERN.matcher(line);
        if (typeMatcher.find()) {
            try {
                builder.withLineType(GPlotSeries.LineType.valueOf(typeMatcher.group(1).toUpperCase(Locale.getDefault())));
                return true;
            } catch (IllegalArgumentException e) {
                LOGGER.debug("cannot find type for {}", typeMatcher.group(1));
            }
        }
        return false;
    }

    private boolean handleTitle(String line, GPlotSeries.Builder builder) {
        Matcher titleMatcher = TITLE_PATTERN.matcher(line);
        if (titleMatcher.find()) {
            builder.withTitle(titleMatcher.group(1));
            return true;
        }
        return false;
    }

    private boolean handleAxis(String line, GPlotSeries.Builder builder) {
        Matcher axesMatcher = AXIS_PATTERN.matcher(line);
        if (axesMatcher.find()) {
            String axis = axesMatcher.group(1);
            switch (axis) {
                case "1":
                    builder.withAxis(GPlotSeries.Axis.LEFT);
                    break;
                case "2":
                    builder.withAxis(GPlotSeries.Axis.RIGHT);
                    break;
            }
            return true;
        } else {
            builder.withAxis(GPlotSeries.Axis.LEFT);
            return false;
        }
    }

    private Map<String, String> extractSetsFrom(List<String> lines) {
        Map<String, String> out = newHashMap();
        for (String line : lines) {
            Matcher matcher = SETS_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }

            out.put(matcher.group(1), matcher.group(2));
        }
        return out;
    }

    public Map<String, GPlotDefinition> getDefaultGPlotFiles() {
        try {
            URL url = GPlotParser.class.getResource("dummy.txt");
            String scheme = url.getProtocol();
            checkArgument(scheme.equals("jar"));
            return readDefinitionsFromJar(url);
        } catch (Exception e) {
            LOGGER.error("loadDefaultGPlotFiles() - cannot load default files", e);
        }
        return emptyMap();
    }

    private Map<String, GPlotDefinition> readDefinitionsFromJar(URL url) throws IOException, URISyntaxException {
        Map<String, GPlotDefinition> result = newHashMap();
        JarURLConnection con = (JarURLConnection) url.openConnection();
        JarFile archive = con.getJarFile();
        Enumeration<JarEntry> entries = archive.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".gplot")) {
                String filename = entry.getName().substring(entry.getName().lastIndexOf("/") + 1);
                String plotName = filename.substring(0, filename.indexOf("."));
                URL resource = GPlotParser.class.getResource(filename);
                result.put(plotName, parse(Resources.toString(resource, Charsets.UTF_8)));
            }
        }
        return result;
    }
}
