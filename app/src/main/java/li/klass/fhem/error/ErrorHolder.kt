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
package li.klass.fhem.error

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider.getUriForFile
import li.klass.fhem.AndFHEMApplication.Companion.ANDFHEM_MAIL
import li.klass.fhem.AndFHEMApplication.Companion.application
import li.klass.fhem.R
import li.klass.fhem.file.provider.AndFHEMFileProvider
import li.klass.fhem.util.DialogUtil.showAlertDialog
import li.klass.fhem.util.DialogUtil.showConfirmBox
import li.klass.fhem.util.StackTraceUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

object ErrorHolder {
    @Volatile
    @Transient
    private var errorException: Exception? = null
    @Volatile
    @Transient
    private var errorMessage: String? = null

    private val logger: Logger = LoggerFactory.getLogger(ErrorHolder::class.java)

    fun setError(exception: Exception?, message: String?) {
        errorException = exception
        errorMessage = message
    }

    fun setError(errorMessage: String?) {
        setError(null, errorMessage)
    }

    val text: String?
        get() {
            var text = errorMessage
            val exceptionString: String = if (errorException != null) {
                StackTraceUtil.exceptionAsString(errorException)
            } else {
                StackTraceUtil.whereAmI()
            }
            if (errorException != null) {
                text += "\r\n --------- \r\n\r\n$exceptionString"
            }
            return text
        }

    fun sendLastErrorAsMail(activity: Activity) {
        showConfirmBox(activity, R.string.error_send, R.string.error_send_content,
                       Runnable { handleSendLastError(activity) })
    }

    private fun handleSendLastError(activity: Activity) {
        if (!handleExternalStorageState(activity)) return
        try {
            val lastError = text
            if (lastError == null) {
                showAlertDialog(activity, R.string.error_send, R.string.error_send_no_error)
                return
            }
            val attachment = writeToDisk(lastError, activity)
            sendMail(activity, "Error encountered!", deviceInformation(), uriFrom(attachment, activity))
        } catch (e: Exception) {
            logger.error("error while sending last error")
        }
    }

    fun sendApplicationLogAsMail(activity: Activity) {
        if (!handleExternalStorageState(activity)) return
        try {
            val file = writeApplicationLogToDisk(activity)
            sendMail(activity, "Send app log", deviceInformation(),
                    uriFrom(file, activity))
        } catch (e: Exception) {
            logger.error("Error while reading application log", e)
        }
    }

    private fun uriFrom(file: File, context: Context): Uri = getUriForFile(context,
                                                                           AndFHEMFileProvider.AUTHORITY,
                                                                           file)

    @Throws(IOException::class)
    private fun sendMail(activity: Activity?, subject: String, text: String, attachment: Uri) {
        if (activity == null) return
        val intent = Intent(Intent.ACTION_SEND).setType("text/plain")
                .setFlags(FLAG_GRANT_READ_URI_PERMISSION)
                .putExtra(Intent.EXTRA_EMAIL, arrayOf(ANDFHEM_MAIL))
                .putExtra(Intent.EXTRA_SUBJECT, subject).putExtra(Intent.EXTRA_TEXT, text)
                .putExtra(Intent.EXTRA_STREAM, attachment)
        activity.startActivityForResult(intent, 0)
    }

    @Throws(IOException::class)
    private fun writeApplicationLogToDisk(context: Context): File {
        val tempFile = File.createTempFile("andFHEM", ".log").apply { deleteOnExit() }

        val process =
                Runtime.getRuntime().exec("logcat -d -n 8 -r 32 -D -f " + tempFile.absolutePath)
        val resultCode = process.waitFor()
        if (resultCode != 0) {
            val error = process.errorStream.use { it.readBytes().toString(Charsets.UTF_8) }
            throw IllegalStateException("Could not read application log: $error")
        }
        val log = tempFile.readText(Charsets.UTF_8)
        return writeToDisk(log, context)
    }

    private fun getErrorLog(context: Context) = File(File(context.filesDir, "logs"), "error.log")

    @Throws(IOException::class)
    private fun writeToDisk(content: String, context: Context) = getErrorLog(context).apply {
        parent?.let { File(it).mkdirs() }
        createNewFile()
        writeText(content, Charsets.UTF_8)
        deleteOnExit()
    }

    private fun deviceInformation(): String = listOf("Device information:",
                                                     "OS-Version: " + System.getProperty(
                                                             "os.version"),
                                                     "API-Level: " + Build.VERSION.SDK_INT,
                                                     "Device: " + Build.DEVICE,
                                                     "Manufacturer: " + Build.MANUFACTURER,
                                                     "Model: " + Build.MODEL,
                                                     "Product: " + Build.PRODUCT,
                                                     "App-version: " + application!!.currentApplicationVersion).joinToString(
            separator = "\r\n")

    private fun handleExternalStorageState(context: Context): Boolean {
        val state = Environment.getExternalStorageState()
        return when {
            state != Environment.MEDIA_MOUNTED -> {
                showAlertDialog(context, R.string.error, R.string.errorExternalStorageNotPresent)
                false
            }
            else                               -> true
        }
    }
}