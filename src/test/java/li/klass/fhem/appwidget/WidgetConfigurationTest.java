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

package li.klass.fhem.appwidget;

import com.google.common.collect.ImmutableList;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import li.klass.fhem.appwidget.view.WidgetType;

import static com.tngtech.java.junit.dataprovider.DataProviders.$;
import static com.tngtech.java.junit.dataprovider.DataProviders.$$;
import static li.klass.fhem.appwidget.WidgetConfiguration.escape;
import static li.klass.fhem.appwidget.WidgetConfiguration.unescape;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class WidgetConfigurationTest {

    @DataProvider
    public static Object[][] serializationProvider() {
        return $$(
                $(new WidgetConfiguration(5, WidgetType.DIM, ImmutableList.of("abc"), false)),
                $(new WidgetConfiguration(5, WidgetType.DIM, ImmutableList.of("abc"), false)),
                $(new WidgetConfiguration(50000, WidgetType.STATUS, ImmutableList.of("d#ef"), false)),
                $(new WidgetConfiguration(50000, WidgetType.STATUS, ImmutableList.of("def", "hello"), false))
        );
    }

    @Test
    @UseDataProvider("serializationProvider")
    public void should_serialize_correctly(WidgetConfiguration widgetConfiguration) {
        String saveString = widgetConfiguration.toSaveString();
        WidgetConfiguration loaded = WidgetConfiguration.fromSaveString(saveString);
        assertThat(loaded).isEqualTo(widgetConfiguration);
    }

    @Test
    public void should_escape_hashes_correctly() {
        assertThat(escape("d#ef")).isEqualTo("d\\@ef");
        assertThat(escape("d@ef")).isEqualTo("d@ef");
        assertThat(escape("def")).isEqualTo("def");
        assertThat(escape(null)).isNull();
    }

    @Test
    public void should_unescape_hashes_correctly() {
        assertThat(unescape("d\\@ef")).isEqualTo("d#ef");
        assertThat(unescape("d@ef")).isEqualTo("d@ef");
        assertThat(unescape("def")).isEqualTo("def");
        assertThat(unescape(null)).isNull();
    }


    @DataProvider
    public static Object[][] saveStringToConfigurationProvider() {
        return $$(
                $(new FromSaveStringTestCase()
                        .withSaveString("123#" + WidgetType.STATUS.name() + "#abc")
                        .thenExpect(new WidgetConfiguration(123, WidgetType.STATUS, ImmutableList.of("abc"), true))),
                $(new FromSaveStringTestCase().withSaveString("123#" + WidgetType.STATUS.name())
                        .thenExpect(new WidgetConfiguration(123, WidgetType.STATUS, Collections.<String>emptyList(), true))),
                $(new FromSaveStringTestCase()
                        .withSaveString(
                                "{" +
                                        "\"widgetId\": \"123\", " +
                                        "\"widgetType\": \"STATUS\", " +
                                        "\"payload\": [\"bla\", \"blub\"]" +
                                        "}")
                        .thenExpect(new WidgetConfiguration(123, WidgetType.STATUS, ImmutableList.of("bla", "blub"), false)))
        );
    }

    @UseDataProvider("saveStringToConfigurationProvider")
    @Test
    public void should_deserialize_save_string(FromSaveStringTestCase testCase) {
        WidgetConfiguration configuration = WidgetConfiguration.fromSaveString(testCase.saveString);
        assertThat(configuration).isEqualTo(testCase.expectedConfiguration);
    }

    private static class FromSaveStringTestCase {
        String saveString;
        WidgetConfiguration expectedConfiguration;

        public FromSaveStringTestCase withSaveString(String saveString) {
            this.saveString = saveString;
            return this;
        }

        public FromSaveStringTestCase thenExpect(WidgetConfiguration expectedConfiguration) {
            this.expectedConfiguration = expectedConfiguration;
            return this;
        }
    }
}