/*
 *  AndFHEM - Open Source Android application to control a FHEM home automation
 *  server.
 *
 *  Copyright (c) 2011, Matthias Klass or third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by Red Hat Inc.
 *
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 *  for more details.
 *
 *  You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 *  along with this distribution; if not, write to:
 *    Free Software Foundation, Inc.
 *    51 Franklin Street, Fifth Floor
 *    Boston, MA  02110-1301  USA
 */

package li.klass.fhem.log

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.google.common.io.Files
import kotlinx.serialization.toUtf8Bytes
import li.klass.fhem.appwidget.update.AppWidgetInstanceManager
import li.klass.fhem.connection.backend.DataConnectionSwitch
import li.klass.fhem.connection.backend.FHEMWEBConnection
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.util.DateFormatUtil
import li.klass.fhem.util.io.FileSystemService
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory
import java.io.File
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class FhemLogService @Inject constructor(private val dataConnectionSwitch: DataConnectionSwitch,
                                         private val application: Application,
                                         private val fileSystemService: FileSystemService,
                                         private val deviceListService: DeviceListService) {

    @SuppressLint("SetWorldReadable")
    fun getLogAndWriteToTemporaryFile(): File? {
        return getLog()?.let { content: String ->
            val outputFile = File(directory, "fhem.log")
            outputFile.setReadable(true, false)
            outputFile.deleteOnExit()

            Files.write(content.toUtf8Bytes(), outputFile)
            outputFile
        }
    }

    private fun getLog(): String? {
        return try {
            val today = LocalDate.now()
            val thisMonth = "fhem-${today.toString(yearMonthDateFormat)}.log";

            val logfileName = (deviceListService.getDeviceForName("Logfile")
                    ?.xmlListDevice?.getInternal("currentlogfile")
                    ?.replace("./log/", "")
                    ?: thisMonth)

            val fhemWebConnection = dataConnectionSwitch.getProviderFor() as FHEMWEBConnection
            val content = fhemWebConnection.request(
                    applicationContext,
                    "/FileLog_logWrapper?dev=Logfile&type=text&file=$logfileName"
            ).content

            content?.let {
                val todayWithDots = today.toString(yearMonthDateFormatWithDot)
                it
                        .replace(Regex("<br/>.+?(?=</html>)</html>"), "")
                        .replace(Regex("^.+?(?=<br>${todayWithDots})<br>", RegexOption.DOT_MATCHES_ALL), "")
            }
        } catch (e: Exception) {
            LOG.error("getLog - cannot retrieve application log", e)
            null
        }
    }

    private val applicationContext: Context get() = application.applicationContext

    val directory: File
        get() = fileSystemService.getOrCreateDirectoryIn(fileSystemService.documentsFolder, "andFHEM")

    companion object {
        private val LOG = LoggerFactory.getLogger(AppWidgetInstanceManager::class.java)!!
        private val yearMonthDateFormat = DateTimeFormat.forPattern("yyyy-MM")
        private val yearMonthDateFormatWithDot = DateTimeFormat.forPattern("yyyy.MM")
    }
}