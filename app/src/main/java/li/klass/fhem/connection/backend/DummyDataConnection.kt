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
package li.klass.fhem.connection.backend

import android.content.Context
import com.google.common.base.Charsets
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.DateFormatUtil
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory
import java.io.IOException

class DummyDataConnection internal constructor(fhemServerSpec: FHEMServerSpec?, applicationProperties: ApplicationProperties?) : FHEMConnection(fhemServerSpec!!, applicationProperties!!) {
    override fun executeCommand(command: String, context: Context): RequestResult<String> {
        LOG.error("executeCommand() - execute command {}", command)
        if (command.startsWith("xmllist")) return xmllist()
        if (command.startsWith("get")) return fileLogData(command)
        return if (command == "{{ TimeNow() }}") RequestResult.Success(DateFormatUtil.FHEM_DATE_FORMAT.print(DateTime.now())) else RequestResult.Success("I am a dummy. Do you expect me to answer you?")
    }

    private fun xmllist(): RequestResult<String> {
        return try {
            LOG.info("xmllist() - loading xmllist")
            val dummyServerSpec = server as DummyServerSpec
            DummyDataConnection::class.java.getResource(dummyServerSpec.fileName)
                    ?.readText(Charsets.UTF_8)
                    ?.replace(" {2}".toRegex(), "")
                    ?.let { RequestResult.Success(it) }
                    ?: RequestResult.Error(RequestResultError.INTERNAL_ERROR)
        } catch (e: IOException) {
            LOG.error("xmllist() - cannot read file", e)
            throw RuntimeException(e)
        }
    }

    private fun fileLogData(command: String): RequestResult<String> {
        val lastSpace = command.lastIndexOf(" ")
        val columnSpec = command.substring(lastSpace + 1)
        val today = FORMATTER.print(DateTime())
        val content = """
            ${today}_00:16:48 4.2
            ${today}_01:19:21 5.2
            ${today}_02:21:53 5.2
            ${today}_03:24:26 6.2
            ${today}_04:26:58 7.3
            ${today}_05:32:03 8.2
            ${today}_06:37:08 9.3
            ${today}_07:39:41 8.3
            ${today}_08:42:13 6.3
            ${today}_09:44:46 5.3
            ${today}_10:49:51 4.3
            ${today}_11:52:23 3.3
            ${today}_12:54:56 2.3
            ${today}_13:57:28 1.3
            ${today}_14:00:28 -1.3
            ${today}_15:57:28 -2.3
            ${today}_16:57:28 -3.3
            ${today}_17:57:28 -4.3
            #$columnSpec
            """.trimIndent()
        return RequestResult.Success(content)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DummyDataConnection::class.java)
        private val FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd")
    }
}