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

package li.klass.fhem.backup

import com.google.common.collect.ImmutableMap
import li.klass.fhem.devices.list.favorites.backend.FavoritesService
import li.klass.fhem.testutil.MockitoRule
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.io.FileSystemService
import li.klass.fhem.util.preferences.SharedPreferencesService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock

@Suppress("unused")
class ImportExportServiceTest {
    @Rule
    @JvmField
    var mockitoRule = MockitoRule()

    @Mock
    lateinit var applicationProperties: ApplicationProperties
    @Mock
    lateinit var favoritesService: FavoritesService
    @Mock
    lateinit var sharedPreferencesService: SharedPreferencesService
    @Mock
    lateinit var fileSystemService: FileSystemService

    @InjectMocks
    lateinit var importExportService: ImportExportService

    @Test
    fun should_match_export_and_import_values() {
        // given
        val values = ImmutableMap.builder<String, Any>()
                .put("a", "1")
                .put("b", 1)
                .put("c", 1.0)
                .put("f", 1f)
                .put("d", "1.0")
                .put("e", "anc")
                .put("g", "anc/bas")
                .put("h", true)
                .build()

        // when
        val converted = importExportService.toImportValues(importExportService.toExportValues(values)) as Map<*, *>

        // then
        assertThat(converted).isEqualTo(values)
    }

    @Test
    fun should_export_values() {
        // given
        val values = ImmutableMap.builder<String, Any>()
                .put("a", "1")
                .put("b", 1)
                .put("c", 1.0)
                .put("f", 1.0f)
                .put("d", "1.0")
                .put("e", "anc")
                .put("g", "anc/bas")
                .put("h", true)
                .build()

        // when
        val exported = importExportService.toExportValues(values)

        // then
        assertThat(exported).isEqualTo(ImmutableMap.builder<String, String>()
                .put("a", "1/java.lang.String")
                .put("b", "1/java.lang.Integer")
                .put("c", "1.0/java.lang.Double")
                .put("f", "1.0/java.lang.Float")
                .put("d", "1.0/java.lang.String")
                .put("e", "anc/java.lang.String")
                .put("g", "anc/bas/java.lang.String")
                .put("h", "true/java.lang.Boolean")
                .build())
    }

    @Test
    fun should_import_values() {
        val values = ImmutableMap.builder<String, String>()
                .put("a", "1/java.lang.String")
                .put("b", "1/java.lang.Integer")
                .put("c", "1.0/java.lang.Double")
                .put("f", "1.0/java.lang.Float")
                .put("d", "1.0/java.lang.String")
                .put("e", "anc/java.lang.String")
                .put("g", "anc/bas/java.lang.String")
                .put("h", "true/java.lang.Boolean")
                .build()

        // when
        val imported = importExportService.toImportValues(values) as Map<*, *>

        // then
        assertThat(imported).isEqualTo(ImmutableMap.builder<String, Any>()
                .put("a", "1")
                .put("b", 1)
                .put("c", 1.0)
                .put("f", 1.0f)
                .put("d", "1.0")
                .put("e", "anc")
                .put("g", "anc/bas")
                .put("h", true)
                .build())
    }
}