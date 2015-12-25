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

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;

import static li.klass.fhem.service.graph.gplot.GPlotDefinitionTestdataBuilder.defaultGPlotDefinition;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class SvgGraphDefinitionTest {
    @DataProvider
    public static Object[][] datapoint() {
        return new Object[][]{
                {
                        new SvgGraphDefinition("bla", defaultGPlotDefinition(), null, Arrays.asList("bla", "blub"), "title", Collections.<String>emptyList()),
                        "myText", "myText"
                },
                {
                        new SvgGraphDefinition("bla", defaultGPlotDefinition(), null, Arrays.asList("bla", "blub"), "title", Collections.<String>emptyList()),
                        "myText<L1>", "myTextbla"
                }, {
                new SvgGraphDefinition("bla", defaultGPlotDefinition(), null, Arrays.asList("bla", "blub"), "title", Collections.<String>emptyList()),
                "myText<L3>", "myText"
        },
        };
    }

    @Test
    @UseDataProvider("datapoint")
    public void should_replace_text(SvgGraphDefinition graphDefinition, String toFormat, String expectedText) throws Exception {
        assertThat(graphDefinition.formatText(toFormat)).isEqualTo(expectedText);
    }
}