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

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.Range;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

@Singleton
public class GPlotParser {

    public static final Pattern SETS_PATTERN = Pattern.compile("set ([a-zA-Z0-9]+) [\"'\\[]([^\"^\']+)[\"'\\]]");
    public static final Pattern AXIS_PATTERN = Pattern.compile("axes x1y([12])");
    public static final Pattern TITLE_PATTERN = Pattern.compile("title '([^']*)'");
    public static final Pattern TYPE_PATTERN = Pattern.compile("with ([a-zA-Z]+)");

    private static final Logger LOGGER = LoggerFactory.getLogger(GPlotParser.class);

    public static final FilenameFilter GPLOT_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            return filename != null && filename.endsWith(".gplot");
        }
    };

    @Inject
    public GPlotParser() {
    }

    public Optional<GPlotDefinition> parseSafe(String content) {
        try {
            return Optional.of(parse(content));
        } catch (Exception e) {
            LOGGER.info("parseSafe() - cannot parse: \r\n" + content, e);
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
            String rangeValue = setsDeclarations.get(rangeKey);
            String[] parts = rangeValue.split(":");

            Range<Double> range;
            if (rangeValue.startsWith(":")) {
                range = Range.atMost(Double.parseDouble(parts[0]));
            } else if (rangeValue.endsWith(":")) {
                range = Range.atLeast(Double.parseDouble(parts[0]));
            } else {
                range = Range.closed(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
            }
            optRange = Optional.of(range);
        }

        return new GPlotAxis(rightLabel, optRange);
    }

    @SuppressWarnings("ConstantConditions")
    private List<GPlotSeries> extractSeriesFrom(List<String> lines) {
        List<GPlotSeries> result = newArrayList();
        Queue<GPlotSeries.Builder> builders = new LinkedList<>();

        boolean plotFound = false;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("plot")) {
                plotFound = true;
            }
            if (line.startsWith("#FileLog ") || line.startsWith("#Log.") || line.startsWith("#logProxy")) {
                builders.add(new GPlotSeries.Builder().withFileLogDef(line.split(" ")[1]));
            } else if (line.startsWith("#DbLog ")) {
                builders.add(new GPlotSeries.Builder().withDbLogDef(line.split(" ")[1]));
            } else if (plotFound) {
                GPlotSeries.Builder builder = builders.peek();
                if (builder == null) {
                    break;
                }

                boolean attributeFound = handleAxis(line, builder);
                attributeFound = handleTitle(line, builder) | attributeFound;
                attributeFound = handleType(line, builder) | attributeFound;

                if (attributeFound) {
                    result.add(builder.build());
                    builders.remove();
                }
            }
        }

        return result;
    }

    private boolean handleType(String line, GPlotSeries.Builder builder) {
        Matcher typeMatcher = TYPE_PATTERN.matcher(line);
        if (typeMatcher.find()) {
            try {
                builder.withType(GPlotSeries.Type.valueOf(typeMatcher.group(1).toUpperCase(Locale.getDefault())));
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
        Map<String, GPlotDefinition> result = newHashMap();
        try {
            File resourceDirectory = new File(GPlotParser.class.getResource(".").toURI());
            File[] files = resourceDirectory.listFiles(GPLOT_FILTER);
            for (File file : files) {
                String name = file.getName().substring(0, file.getName().indexOf("."));
                result.put(name, parse(Files.toString(file, Charsets.UTF_8)));
            }
        } catch (Exception e) {
            LOGGER.error("loadDefaultGPlotFiles() - cannot load default files", e);
        }
        return result;
    }
}
