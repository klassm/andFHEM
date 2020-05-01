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
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.CloseableUtil
import li.klass.fhem.util.StringUtil
import org.apache.commons.net.telnet.TelnetClient
import org.slf4j.LoggerFactory
import java.io.*
import java.net.SocketException
import java.net.SocketTimeoutException

class TelnetConnection internal constructor(fhemServerSpec: FHEMServerSpec?, applicationProperties: ApplicationProperties?) : FHEMConnection(fhemServerSpec!!, applicationProperties!!) {
    override fun executeCommand(command: String, context: Context): RequestResult<String> {
        LOG.info("executeTask command {}", command)
        val telnetClient = TelnetClient()
        telnetClient.connectTimeout = connectionTimeoutMilliSeconds
        var bufferedOutputStream: BufferedOutputStream? = null
        var printStream: PrintStream? = null
        val errorHost = server.ip + ":" + server.port
        return try {
            telnetClient.connect(server.ip, server.port)
            val outputStream = telnetClient.outputStream
            val inputStream = telnetClient.inputStream
            bufferedOutputStream = BufferedOutputStream(outputStream)
            printStream = PrintStream(outputStream)
            waitForFilledStream(inputStream, 5000)
            val passwordRead = readUntil(inputStream, PASSWORD_PROMPT)
            if (passwordRead != null && passwordRead.contains(PASSWORD_PROMPT)) {
                LOG.info("sending password")
                if (!writeCommand(inputStream, printStream, server.password)) {
                    return RequestResult.Error(RequestResultError.AUTHENTICATION_ERROR)
                }
            }
            if (!writeCommand(inputStream, printStream, command + "\r\n")) {
                return RequestResult.Error(RequestResultError.HOST_CONNECTION_ERROR)
            }

            // If we send an xmllist, we are done when finding the closing FHZINFO tag.
            // If another command is used, the tag ending delimiter is obsolete, not found and
            // therefore not used. We just read until the stream ends.
            var result: String?
            result = if (command == "xmllist") {
                readUntil(inputStream, "</FHZINFO>")
            } else {
                read(inputStream)
            }
            if (result == null) {
                return RequestResult.Error(RequestResultError.INVALID_CONTENT)
            }
            telnetClient.disconnect()
            var startPos = result.indexOf(", try help")
            if (startPos != -1) {
                result = result.substring(startPos + ", try help".length)
            }
            startPos = result.indexOf("<")
            if (startPos != -1) {
                result = result.substring(startPos)
            }
            result = result
                    .replace("Bye...".toRegex(), "")
                    .replace("fhem>".toRegex(), "")
            result = String(result.toByteArray(charset("UTF8")))
            LOG.debug("result is {}", result)
            RequestResult.Success(result)
        } catch (e: SocketTimeoutException) {
            LOG.error("timeout", e)
            setErrorInErrorHolderFor(e, errorHost, command)
            RequestResult.Error(RequestResultError.CONNECTION_TIMEOUT)
        } catch (e: UnsupportedEncodingException) {
            // this may never happen, as UTF8 is known ...
            setErrorInErrorHolderFor(e, errorHost, command)
            throw IllegalStateException("unsupported encoding", e)
        } catch (e: SocketException) {
            // We handle host connection errors directly after connecting to the server by waiting
            // for some token for some seconds. Afterwards, the only possibility for an error
            // is that the FHEM server ends the connection after receiving an invalid password.
            LOG.error("SocketException", e)
            setErrorInErrorHolderFor(e, errorHost, command)
            RequestResult.Error(RequestResultError.AUTHENTICATION_ERROR)
        } catch (e: IOException) {
            LOG.error("IOException", e)
            setErrorInErrorHolderFor(e, errorHost, command)
            RequestResult.Error(RequestResultError.HOST_CONNECTION_ERROR)
        } finally {
            CloseableUtil.close(printStream, bufferedOutputStream)
        }
    }

    @Throws(IOException::class)
    private fun readUntil(inputStream: InputStream, vararg blockers: String): String? {
        val buffer = StringBuilder()
        while (true) {
            if (inputStream.available() == 0 && !waitForFilledStream(inputStream, 5000)) {
                LOG.error("read data, but did not find end token, read content was '{}'", buffer.toString())
                return null
            }
            val readChar = inputStream.read().toChar()
            buffer.append(readChar)
            for (blocker in blockers) {
                if (StringUtil.endsWith(buffer, blocker)) return buffer.toString()
            }
        }
    }

    @Throws(IOException::class)
    private fun writeCommand(inputStream: InputStream, printStream: PrintStream, command: String?): Boolean {
        LOG.debug("sending command {}", command)
        printStream.println(command)
        printStream.flush()
        return waitForFilledStream(inputStream, 5000)
    }

    @Throws(IOException::class)
    private fun waitForFilledStream(inputStream: InputStream, timeToWait: Int): Boolean {
        val initialFill = inputStream.available()
        val startTime = System.currentTimeMillis()
        while (inputStream.available() == initialFill &&
                System.currentTimeMillis() - startTime < timeToWait) {
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                LOG.debug("interrupted, ignoring", e)
            }
        }
        return inputStream.available() > 0
    }

    @Throws(IOException::class)
    private fun read(inputStream: InputStream): String {
        waitForFilledStream(inputStream, 3000)
        val buffer = StringBuilder()
        while (inputStream.available() > 0 || waitForFilledStream(inputStream, 100)) {
            val readChar = inputStream.read().toChar()
            buffer.append(readChar)
        }
        return buffer.toString()
    }

    companion object {
        val TAG = TelnetConnection::class.java.name
        private const val PASSWORD_PROMPT = "Password: "
        private val LOG = LoggerFactory.getLogger(TelnetConnection::class.java.name)
    }
}