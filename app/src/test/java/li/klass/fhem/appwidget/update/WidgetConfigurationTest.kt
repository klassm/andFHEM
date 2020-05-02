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

package li.klass.fhem.appwidget.update

import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.DataProviders.testForEach
import com.tngtech.java.junit.dataprovider.UseDataProvider
import li.klass.fhem.appwidget.ui.widget.WidgetType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(DataProviderRunner::class)
class WidgetConfigurationTest {

    @Test
    @UseDataProvider("serializationProvider")
    fun should_serialize_correctly(widgetConfiguration: WidgetConfiguration) {
        val saveString = widgetConfiguration.toSaveString()
        val loaded = WidgetConfiguration.Companion.fromSaveString(saveString)
        assertThat(loaded).isEqualTo(widgetConfiguration)
    }

    @Test
    fun should_escape_hashes_correctly() {
        assertThat(WidgetConfiguration.Companion.escape("d#ef")).isEqualTo("d\\@ef")
        assertThat(WidgetConfiguration.Companion.escape("d@ef")).isEqualTo("d@ef")
        assertThat(WidgetConfiguration.Companion.escape("def")).isEqualTo("def")
        assertThat(WidgetConfiguration.Companion.escape(null)).isNull()
    }

    @Test
    fun should_unescape_hashes_correctly() {
        assertThat(WidgetConfiguration.Companion.unescape("d\\@ef")).isEqualTo("d#ef")
        assertThat(WidgetConfiguration.Companion.unescape("d@ef")).isEqualTo("d@ef")
        assertThat(WidgetConfiguration.Companion.unescape("def")).isEqualTo("def")
        assertThat(WidgetConfiguration.Companion.unescape(null)).isNull()
    }

    companion object {

        @DataProvider
        @JvmStatic
        fun serializationProvider(): Array<Array<Any>> {
            return testForEach(
                    WidgetConfiguration(5, WidgetType.DIM, "connectionId", listOf("abc")),
                    WidgetConfiguration(5, WidgetType.DIM, null, listOf("abc")),
                    WidgetConfiguration(50000, WidgetType.STATUS, "connectionIdla", listOf("d#ef")),
                    WidgetConfiguration(50000, WidgetType.STATUS, "connectionIdBlo", listOf("def", "hello"))
            )
        }
    }
}