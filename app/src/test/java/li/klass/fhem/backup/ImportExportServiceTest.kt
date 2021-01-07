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

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import li.klass.fhem.devices.list.favorites.backend.FavoritesService
import li.klass.fhem.testutil.MockRule
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.io.FileSystemService
import li.klass.fhem.util.preferences.SharedPreferencesService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

@Suppress("unused")
class ImportExportServiceTest {
    @Rule
    @JvmField
    var mockitoRule = MockRule()

    @MockK
    lateinit var applicationProperties: ApplicationProperties

    @MockK
    lateinit var favoritesService: FavoritesService

    @MockK
    lateinit var sharedPreferencesService: SharedPreferencesService

    @MockK
    lateinit var fileSystemService: FileSystemService

    @InjectMockKs
    lateinit var importExportService: ImportExportService

    @Test
    fun should_match_export_and_import_values() {
        // given
        val values = mapOf<String, Any>(
                "a" to "1",
                "b" to 1,
                "c" to 1.0,
                "f" to 1f,
                "d" to "1.0",
                "e" to "anc",
                "g" to "anc/bas",
                "h" to true
        )

        // when
        val converted = importExportService.toImportValues(importExportService.toExportValues(values)) as Map<*, *>

        // then
        assertThat(converted).isEqualTo(values)
    }

    @Test
    fun should_export_values() {
        // given
        val values = mapOf<String, Any>(
                "a" to "1",
                "b" to 1,
                "c" to 1.0,
                "f" to 1.0f,
                "d" to "1.0",
                "e" to "anc",
                "g" to "anc/bas",
                "h" to true,
                "l" to 1L
        )

        // when
        val exported = importExportService.toExportValues(values)

        // then
        assertThat(exported).isEqualTo(mapOf(
                "a" to "1/java.lang.String",
                "b" to "1/java.lang.Integer",
                "c" to "1.0/java.lang.Double",
                "f" to "1.0/java.lang.Float",
                "d" to "1.0/java.lang.String",
                "e" to "anc/java.lang.String",
                "g" to "anc/bas/java.lang.String",
                "h" to "true/java.lang.Boolean",
                "l" to "1/java.lang.Long"
        ))
    }

    @Test
    fun should_import_values() {
        val values = mapOf(
                "a" to "1/java.lang.String",
                "b" to "1/java.lang.Integer",
                "c" to "1.0/java.lang.Double",
                "f" to "1.0/java.lang.Float",
                "d" to "1.0/java.lang.String",
                "e" to "anc/java.lang.String",
                "g" to "anc/bas/java.lang.String",
                "h" to "true/java.lang.Boolean",
                "l" to "1/java.lang.Long"
        )

        // when
        val imported = importExportService.toImportValues(values) as Map<*, *>

        // then
        assertThat(imported).isEqualTo(mapOf(
                "a" to "1",
                "b" to 1,
                "c" to 1.0,
                "f" to 1.0f,
                "d" to "1.0",
                "e" to "anc",
                "g" to "anc/bas",
                "h" to true,
                "l" to 1L
        ))
    }
}