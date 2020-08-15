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

import android.content.Context
import com.google.gson.Gson
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.devices.list.favorites.backend.FavoritesService
import li.klass.fhem.service.NotificationService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.CloseableUtil
import li.klass.fhem.util.Logging
import li.klass.fhem.util.ReflectionUtil
import li.klass.fhem.util.io.FileSystemService
import li.klass.fhem.util.preferences.SharedPreferencesService
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import javax.inject.Inject

class ImportExportService @Inject constructor(
        private val sharedPreferencesService: SharedPreferencesService,
        private val fileSystemService: FileSystemService,
        private val favoritesService: FavoritesService,
        private val applicationProperties: ApplicationProperties
) : Logging {
    private val backupFileName: String
        get() = "andFHEM-" + dateTimeFormatter.print(DateTime.now()) + ".backup"

    val exportDirectory: File
        get() = fileSystemService.getOrCreateDirectoryIn(fileSystemService.documentsFolder, "andFHEM")

    enum class ImportStatus {
        SUCCESS, INVALID_FILE, WRONG_PASSWORD
    }

    private fun getSharedPreferencesExportKeys(context: Context): Map<String, String> {
        val builder = mutableMapOf(
                "CONNECTIONS" to ConnectionService.PREFERENCES_NAME,
                "NOTIFICATIONS" to NotificationService.PREFERENCES_NAME,
                "DEFAULT" to applicationProperties.getApplicationSharedPreferencesName(context)
        )
        for (preferenceName in favoritesService.getPreferenceNames()) {
            builder["FAVORITE_$preferenceName"] = preferenceName
        }
        return builder.toMap()
    }

    fun exportSettings(password: String?, context: Context): File {
        val toExport = mutableMapOf<String, Map<String, *>>()

        for ((key, value) in getSharedPreferencesExportKeys(context)) {
            val values = sharedPreferencesService.listAllFrom(value)
            toExport[key] = toExportValues(values)
        }

        return createZipFrom(toExport.toMap(), password)
    }

    fun toExportValues(values: Map<String, *>) = values.entries
            .map { it.key to it.value.toString() + "/" + (it.value as Any).javaClass.name }
            .toMap()


    fun toImportValues(values: Map<String, String>): Map<String, *> {
        val toImport = mutableMapOf<String, Any>()
        for ((key, value1) in values) {
            val separator = value1.lastIndexOf("/")
            val clazz = ReflectionUtil.classForName(value1.substring(separator + 1))
            val value = value1.substring(0, separator)
            toImport[key] = typedValueFor(value, clazz)
        }
        return toImport
    }

    private fun typedValueFor(value: String, type: Class<*>): Any {
        return if (type.isAssignableFrom(Int::class.java) || type.isAssignableFrom(Integer::class.java)) {
            Integer.parseInt(value)
        } else if (type.isAssignableFrom(Float::class.java) || type.isAssignableFrom(java.lang.Float::class.java)) {
            java.lang.Float.parseFloat(value)
        } else if (type.isAssignableFrom(Boolean::class.java) || type.isAssignableFrom(java.lang.Boolean::class.java)) {
            java.lang.Boolean.parseBoolean(value)
        } else if (type.isAssignableFrom(String::class.java)) {
            value
        } else if (type.isAssignableFrom(Double::class.java) || type.isAssignableFrom(java.lang.Double::class.java)) {
            java.lang.Double.parseDouble(value)
        } else if (type.isAssignableFrom(Long::class.java) || type.isAssignableFrom(
                        java.lang.Long::class.java)) {
            java.lang.Long.parseLong(value)
        } else {
            throw IllegalArgumentException("don't know how to handle " + type.name)
        }
    }

    fun toZipFile(file: File): ZipFile? {
        return try {
            ZipFile(file).takeIf {
                it.isValidZipFile
            }
        } catch (e: ZipException) {
            logger.error("error while reading zip file ${file.absolutePath}", e)
            null
        }
    }

    fun importSettings(zipFile: ZipFile, password: String?, context: Context): ImportStatus {
        try {
            if (zipFile.isEncrypted) {
                zipFile.setPassword((password ?: "").toCharArray())
            }

            if (zipFile.getFileHeader(SHARED_PREFERENCES_FILE_NAME) == null) {
                return ImportStatus.INVALID_FILE
            }

            zipFile.extractFile(SHARED_PREFERENCES_FILE_NAME, fileSystemService.getCacheDir(context).absolutePath)

            val content = InputStreamReader(FileInputStream(File(fileSystemService.getCacheDir(context), SHARED_PREFERENCES_FILE_NAME)))
                    .use { reader ->
                        Gson().fromJson<Map<String, Map<String, String>>>(reader, Map::class.java)
                    }

            importNonFavorites(context, content)
            importFavorites(context, content)

            return ImportStatus.SUCCESS

        } catch (e: ZipException) {
            logger.error("importSettings(${zipFile.file.absolutePath}) - cannot import", e)
            return if (e.type == ZipException.Type.WRONG_PASSWORD || (e.message
                            ?: "").contains("Wrong Password")) {
                ImportStatus.WRONG_PASSWORD
            } else {
                ImportStatus.INVALID_FILE
            }
        } catch (e: Exception) {
            logger.error("importSettings(${zipFile.file.absolutePath}) - cannot import", e)
            return ImportStatus.INVALID_FILE
        }
    }

    private fun importNonFavorites(context: Context, content: Map<String, Map<String, String>>) =
            import(context, content) { !it.startsWith("FAVORITE")}

    private fun importFavorites(context: Context, content: Map<String, Map<String, String>>) =
            import(context, content) { it.startsWith("FAVORITE")}


    private fun import(context: Context, content: Map<String, Map<String, String>>, predicate: (String) -> Boolean) {
        val exportKeys = getSharedPreferencesExportKeys(context)
        content.entries
                .filter { predicate(it.key) }
                .filter { exportKeys.containsKey(it.key) }
                .map { exportKeys.getValue(it.key) to toImportValues(it.value) }
                .forEach { sharedPreferencesService.writeAllIn(it.first, it.second) }
    }

    private fun createZipFrom(toExport: Map<String, Map<String, *>>, password: String?): File {

        var stream: ByteArrayInputStream? = null
        try {
            val exportedJson = Gson().toJson(toExport)

            val exportFile = File(exportDirectory, backupFileName)
            logger.info("export file location is {}", exportFile.absolutePath)
            val zipFile = ZipFile(exportFile, password?.toCharArray())


            val parameters = ZipParameters()
            parameters.compressionMethod = CompressionMethod.DEFLATE
            parameters.fileNameInZip = SHARED_PREFERENCES_FILE_NAME
            if (password != null) {
                parameters.isEncryptFiles = true
                parameters.encryptionMethod = EncryptionMethod.ZIP_STANDARD
            }

            stream = ByteArrayInputStream(exportedJson.toByteArray(Charsets.UTF_8))
            zipFile.addStream(stream, parameters)

            return exportFile
        } catch (e: ZipException) {
            logger.error("cannot create zip", e)
            throw IllegalStateException(e)
        } finally {
            CloseableUtil.close(stream)
        }
    }

    companion object {
        private val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm")!!
        private const val SHARED_PREFERENCES_FILE_NAME = "sharedPreferences.json"
    }
}
