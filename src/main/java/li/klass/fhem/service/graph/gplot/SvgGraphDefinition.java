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

import com.google.common.collect.ComparisonChain;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SvgGraphDefinition implements Serializable {

    private static final Pattern LABEL_PATTERN = Pattern.compile("<L([0-9]+)>");
    public static final Comparator<SvgGraphDefinition> BY_NAME = new Comparator<SvgGraphDefinition>() {
        @Override
        public int compare(SvgGraphDefinition o1, SvgGraphDefinition o2) {
            return ComparisonChain.start().compare(o1.getName(), o2.getName()).result();
        }
    };

    private final String name;
    private final GPlotDefinition gPlotDefinition;
    private final String logDeviceName;
    private final List<String> labels;

    private final String title;
    private final List<String> plotfunction;

    public SvgGraphDefinition(String name, GPlotDefinition gPlotDefinition, String logDeviceName, List<String> labels, String title, List<String> plotfunction) {
        this.name = name;
        this.gPlotDefinition = gPlotDefinition;
        this.logDeviceName = logDeviceName;
        this.labels = labels;
        this.title = title;
        this.plotfunction = plotfunction;
    }

    public String getName() {
        return name;
    }

    public GPlotDefinition getPlotDefinition() {
        return gPlotDefinition;
    }

    public String getLogDeviceName() {
        return logDeviceName;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getPlotfunction() {
        return plotfunction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SvgGraphDefinition that = (SvgGraphDefinition) o;

        return !(name != null ? !name.equals(that.name) : that.name != null)
                && !(gPlotDefinition != null ? !gPlotDefinition.equals(that.gPlotDefinition) : that.gPlotDefinition != null)
                && !(logDeviceName != null ? !logDeviceName.equals(that.logDeviceName) : that.logDeviceName != null)
                && !(labels != null ? !labels.equals(that.labels) : that.labels != null)
                && !(title != null ? !title.equals(that.title) : that.title != null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (gPlotDefinition != null ? gPlotDefinition.hashCode() : 0);
        result = 31 * result + (logDeviceName != null ? logDeviceName.hashCode() : 0);
        result = 31 * result + (labels != null ? labels.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SvgGraphDefinition{" +
                "name='" + name + '\'' +
                ", gPlotDefinition=" + gPlotDefinition +
                ", logDeviceName=" + logDeviceName +
                ", labels=" + labels +
                ", title='" + title + '\'' +
                ", plotfunction='" + plotfunction + '\'' +
                '}';
    }

    public String formatText(String toFormat) {
        String result = toFormat.replaceAll("<TL>", title);
        Matcher matcher = LABEL_PATTERN.matcher(toFormat);
        while (matcher.find()) {
            int labelIndex = Integer.parseInt(matcher.group(1)) - 1;
            String replaceBy = labelIndex < labels.size() ? labels.get(labelIndex).trim() : "";
            result = result.replaceAll(matcher.group(0), replaceBy);
        }
        return result;
    }
}
