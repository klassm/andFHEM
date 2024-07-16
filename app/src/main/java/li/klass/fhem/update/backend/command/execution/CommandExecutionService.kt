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

package li.klass.fhem.update.backend.command.execution

import android.app.Application
import android.content.Context
import android.content.Intent
import li.klass.fhem.connection.backend.DataConnectionSwitch
import li.klass.fhem.connection.backend.FHEMWEBConnection
import li.klass.fhem.connection.backend.RequestResult
import li.klass.fhem.connection.backend.RequestResultError
import li.klass.fhem.connection.backend.RequestResultError.CONNECTION_TIMEOUT
import li.klass.fhem.connection.backend.RequestResultError.HOST_CONNECTION_ERROR
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG
import li.klass.fhem.constants.Actions.SHOW_EXECUTING_DIALOG
import li.klass.fhem.service.AbstractService
import li.klass.fhem.settings.SettingsKeys.COMMAND_EXECUTION_RETRIES
import li.klass.fhem.util.ApplicationProperties
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandExecutionService @Inject constructor(
        val dataConnectionSwitch: DataConnectionSwitch,
        val applicationProperties: ApplicationProperties,
        val application: Application
) : AbstractService() {

    @Transient private var scheduledExecutorService: ScheduledExecutorService? = null
    @Transient
    var lastFailedCommand: Command? = null
        private set

    fun resendLastFailedCommand() {
        val last = lastFailedCommand
        last ?: return
        lastFailedCommand = null
        executeSafely(last, DO_NOTHING)
    }

    fun executeSync(command: Command): String? {
        val resultListener = SyncResultListener()
        executeSafely(command, 0, resultListener)
        val result = resultListener.getResult()
        LOG.info("executeSync({}) - result is '{}'", command.command, result)
        return result
    }

    fun executeSafely(command: Command, resultListener: ResultListener) {
        executeSafely(command, 0, resultListener)
    }

    private fun executeSafely(command: Command, delay: Int, resultListener: ResultListener) {
        LOG.info("executeSafely(command={}, delay={})", command, delay)
        if (delay == 0) {
            executeImmediately(command, 0, resultListener)
        } else {
            executeDelayed(command, delay, applicationContext, resultListener)
        }
    }

    private fun executeDelayed(command: Command, delay: Int, context: Context, callback: ResultListener) {
        schedule(delay, ResendCommand(command, 0, context, callback))
    }

    private fun executeImmediately(command: Command, currentTry: Int, resultListener: ResultListener) {
        showExecutingDialog()

        val result = execute(command, currentTry, resultListener)
        result.fold(
                onSuccess = { resultListener.onResult(it) },
                onError = {
                    lastFailedCommand = command
                    resultListener.onError()
                }
        )
    }

    private fun showExecutingDialog() {
        applicationContext.sendBroadcast(Intent(SHOW_EXECUTING_DIALOG).apply { setPackage(applicationContext.packageName) })
    }

    private fun execute(command: Command, currentTry: Int, resultListener: ResultListener): RequestResult<String> {
        val currentProvider = dataConnectionSwitch.getProviderFor(command.connectionId)
        val result = currentProvider.executeCommand(command.command, applicationContext)

        LOG.info("execute() - executing command={}, try={}", command, currentTry)

        try {
            result.fold(
                    onSuccess = { sendBroadcastWithAction(Actions.CONNECTION_ERROR_HIDE, applicationContext) },
                    onError = {
                        if (shouldTryResend(command.command, it, currentTry)) {
                            val timeoutForNextTry = secondsForTry(currentTry)

                            val resendCommand = ResendCommand(command, currentTry + 1, applicationContext, resultListener)
                            schedule(timeoutForNextTry, resendCommand)
                        }
                        it.handleError(applicationContext)
                    }
            )
        } finally {
            hideExecutingDialog()
        }
        return result
    }

    fun schedule(timeoutForNextTry: Int, resendCommand: ResendCommand): ScheduledFuture<*> {
        LOG.info("schedule() - schedule {} in {} seconds", resendCommand, timeoutForNextTry)
        return getScheduledExecutorService().schedule(resendCommand, timeoutForNextTry.toLong(), SECONDS)
    }

    private fun shouldTryResend(command: String, result: RequestResultError, currentTry: Int): Boolean {
        return when {
            !command.startsWith("set") && !command.startsWith("attr") -> false
            result != CONNECTION_TIMEOUT && result != HOST_CONNECTION_ERROR -> false
            else -> currentTry <= getNumberOfRetries()
        }
    }

    private fun getScheduledExecutorService(): ScheduledExecutorService {
        if (scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newScheduledThreadPool(1)
        }
        return scheduledExecutorService!!
    }

    private fun hideExecutingDialog() {
        applicationContext.sendBroadcast(Intent(DISMISS_EXECUTING_DIALOG).apply { setPackage(applicationContext.packageName) })
    }

    private fun getNumberOfRetries(): Int {
        return applicationProperties.getIntegerSharedPreference(
                COMMAND_EXECUTION_RETRIES, DEFAULT_NUMBER_OF_RETRIES
        )
    }

    fun executeRequest(relativePath: String, context: Context, connectionId: String?): String? {
        val provider = dataConnectionSwitch.getProviderFor(connectionId) as? FHEMWEBConnection
                ?: return null

        val result = provider.executeRequest(relativePath, context)
        return result.fold(
                onSuccess = { stream ->
                    try {
                        stream.reader(Charsets.UTF_8).use { it.readText() }
                    } catch (e: IOException) {
                        LOG.error("executeRequest() - cannot read stream", e)
                        null
                    }
                },
                onError = { null }
        )
    }

    private class SyncResultListener : SuccessfulResultListener() {
        private var result: String? = null

        override fun onResult(result: String) {
            this.result = result
        }

        fun getResult(): String? = if (result != null) result!!.trim { it <= ' ' } else null
    }

    inner class ResendCommand internal constructor(internal var command: Command, private var currentTry: Int, private val context: Context, private val resultListener: ResultListener) : Runnable {

        override fun run() {
            executeImmediately(command, currentTry, resultListener)
        }

        override fun toString(): String {
            return "ResendCommand{" +
                    ", context=" + context +
                    ", currentTry=" + currentTry +
                    ", command='" + command + '\'' +
                    '}'
        }
    }

    interface ResultListener {
        fun onResult(result: String)

        fun onError()
    }

    abstract class SuccessfulResultListener : ResultListener {
        override fun onError() {}
    }

    private val applicationContext get() = application.applicationContext

    companion object {

        const val DEFAULT_NUMBER_OF_RETRIES = 3

        private val LOG = LoggerFactory.getLogger(CommandExecutionService::class.java)
        private val DO_NOTHING = object : SuccessfulResultListener() {
            override fun onResult(result: String) {}
        }

        fun secondsForTry(executionTry: Int): Int = Math.pow(3.0, executionTry.toDouble()).toInt()
    }
}
