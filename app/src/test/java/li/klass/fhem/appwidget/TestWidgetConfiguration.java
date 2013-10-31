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

import li.klass.fhem.appwidget.view.WidgetType;
import org.junit.Test;

import static li.klass.fhem.appwidget.WidgetConfiguration.escape;
import static li.klass.fhem.appwidget.WidgetConfiguration.unescape;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class TestWidgetConfiguration {

    @Test
    public void testWidgetConfigurationSerialization() {
        assertConfigurationSerialization(new WidgetConfiguration(5, "abc", WidgetType.DIM));
        assertConfigurationSerialization(new WidgetConfiguration(50000, "def", WidgetType.STATUS));
        assertConfigurationSerialization(new WidgetConfiguration(50000, "d#ef", WidgetType.STATUS));

        assertConfigurationSerialization(new WidgetConfiguration(50000, "def", WidgetType.STATUS, "hello"));
        assertConfigurationSerialization(new WidgetConfiguration(50000, "def", WidgetType.STATUS, null));
    }

    @Test
    public void testEscape() {
        assertThat(escape("d#ef"), is("d\\@ef"));
        assertThat(escape("d@ef"), is("d@ef"));
        assertThat(escape("def"), is("def"));
        assertThat(escape(null), is(nullValue()));
    }

    @Test
    public void testUnescape() {
        assertThat(unescape("d\\@ef"), is("d#ef"));
        assertThat(unescape("d@ef"), is("d@ef"));
        assertThat(unescape("def"), is("def"));
        assertThat(unescape(null), is(nullValue()));
    }

    private void assertConfigurationSerialization(WidgetConfiguration widgetConfiguration) {
        String saveString = widgetConfiguration.toSaveString();
        WidgetConfiguration loaded = WidgetConfiguration.fromSaveString(saveString);

        assertThat(loaded.deviceName, is(widgetConfiguration.deviceName));
        assertThat(loaded.payload, is(widgetConfiguration.payload));
        assertThat(loaded.widgetId, is(widgetConfiguration.widgetId));
        assertThat(loaded.widgetType, is(widgetConfiguration.widgetType));
    }
}
