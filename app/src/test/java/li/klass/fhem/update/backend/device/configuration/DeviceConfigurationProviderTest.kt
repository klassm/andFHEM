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

package li.klass.fhem.update.backend.device.configuration

import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.serializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(DataProviderRunner::class)
class DeviceConfigurationProviderTest {
    @OptIn(ImplicitReflectionSerializer::class)
    @Test
    @UseDataProvider("allFilesProvider")
    fun should_parse_all_json_files(file: File) {

        val content = file.readText(Charsets.UTF_8)
        val result = Json(JsonConfiguration.Stable).parse(DeviceConfiguration::class.serializer(), content)

        assertThat(result).`as`(file.name).isNotNull()
    }

    @Test
    fun should_parse_concatenated_json() {
        val configuration = DeviceConfigurationProvider().configurationFor("FS20")
        assertThat(configuration).isNotNull()
    }

    companion object {
        @DataProvider
        @JvmStatic
        @Throws(Exception::class)
        fun allFilesProvider(): List<File> {
            val resourceDirectory = File(DeviceConfiguration::class.java.getResource("/deviceConfiguration")!!.toURI())
            val files = resourceDirectory.listFiles { _, filename -> filename != null && filename.endsWith(".json") }

            return files.toList()
        }

    }
}