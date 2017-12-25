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
import com.google.common.base.Optional
import com.google.common.io.CharStreams
import li.klass.fhem.connection.backend.DataConnectionSwitch
import li.klass.fhem.connection.backend.FHEMWEBConnection
import li.klass.fhem.connection.backend.RequestResult
import li.klass.fhem.connection.backend.RequestResultError.CONNECTION_TIMEOUT
import li.klass.fhem.connection.backend.RequestResultError.HOST_CONNECTION_ERROR
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG
import li.klass.fhem.constants.Actions.SHOW_EXECUTING_DIALOG
import li.klass.fhem.service.AbstractService
import li.klass.fhem.settings.SettingsKeys.COMMAND_EXECUTION_RETRIES
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.CloseableUtil
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStreamReader
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
        return resultListener.getResult()
    }


    fun executeSafely(command: Command, resultListener: ResultListener) {
        executeSafely(command, 0, resultListener)
    }

    private fun executeSafely(command: Command, delay: Int, resultListener: ResultListener) {
        LOG.info("executeSafely(command={}, delay={})", command, delay)
        if (delay == 0) {
            executeImmediately(command, 0, applicationContext, resultListener)
        } else {
            executeDelayed(command, delay, applicationContext, resultListener)
        }
    }

    private fun executeDelayed(command: Command, delay: Int, context: Context, callback: ResultListener) {
        schedule(delay, ResendCommand(command, 0, context, callback))
    }

    private fun executeImmediately(command: Command, currentTry: Int, context: Context, resultListener: ResultListener) {
        showExecutingDialog()

        val result = execute(command, currentTry, resultListener)
        if (result.handleErrors(context)) {
            lastFailedCommand = command
            resultListener.onError()
        } else {
            resultListener.onResult(result.content)
        }
    }

    private fun showExecutingDialog() {
        applicationContext.sendBroadcast(Intent(SHOW_EXECUTING_DIALOG))
    }

    private fun execute(command: Command, currentTry: Int, resultListener: ResultListener): RequestResult<String> {
        val currentProvider = dataConnectionSwitch.getProviderFor(command.connectionId.orNull())
        val result = currentProvider.executeCommand(command.command, applicationContext)

        LOG.info("execute() - executing command={}, try={}", command, currentTry)

        try {
            if (result.error == null) {
                sendBroadcastWithAction(Actions.CONNECTION_ERROR_HIDE, applicationContext)
            } else if (shouldTryResend(command.command, result, currentTry)) {
                val timeoutForNextTry = secondsForTry(currentTry)

                val resendCommand = ResendCommand(command, currentTry + 1, applicationContext, resultListener)
                schedule(timeoutForNextTry, resendCommand)
            }
        } finally {
            hideExecutingDialog()
        }
        return result
    }

    fun schedule(timeoutForNextTry: Int, resendCommand: ResendCommand): ScheduledFuture<*> {
        LOG.info("schedule() - schedule {} in {} seconds", resendCommand, timeoutForNextTry)
        return getScheduledExecutorService().schedule(resendCommand, timeoutForNextTry.toLong(), SECONDS)
    }

    private fun shouldTryResend(command: String, result: RequestResult<*>, currentTry: Int): Boolean {
        if (!command.startsWith("set") && !command.startsWith("attr")) return false
        if (result.error == null) return false
        if (result.error != CONNECTION_TIMEOUT && result.error != HOST_CONNECTION_ERROR)
            return false
        return currentTry <= getNumberOfRetries()
    }

    private fun getScheduledExecutorService(): ScheduledExecutorService {
        if (scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newScheduledThreadPool(1)
        }
        return scheduledExecutorService!!
    }

    private fun hideExecutingDialog() {
        applicationContext.sendBroadcast(Intent(DISMISS_EXECUTING_DIALOG))
    }

    private fun getNumberOfRetries(): Int {
        return applicationProperties.getIntegerSharedPreference(
                COMMAND_EXECUTION_RETRIES, DEFAULT_NUMBER_OF_RETRIES
        )
    }

    fun executeRequest(relativePath: String, context: Context): Optional<String> {
        val provider = dataConnectionSwitch.getProviderFor() as? FHEMWEBConnection ?: return Optional.absent()

        val result = provider.executeRequest(relativePath, context)
        if (result.handleErrors(context)) {
            return Optional.absent()
        }
        return try {
            Optional.of(CharStreams.toString(InputStreamReader(result.content)))
        } catch (e: IOException) {
            LOG.error("executeRequest() - cannot read stream", e)
            Optional.absent()
        } finally {
            CloseableUtil.close(result.content)
        }
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
            executeImmediately(command, currentTry, context, resultListener)
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

        val DEFAULT_NUMBER_OF_RETRIES = 3
        private val IMAGE_CACHE_SIZE = 20

        private val LOG = LoggerFactory.getLogger(CommandExecutionService::class.java)
        private val DO_NOTHING = object : SuccessfulResultListener() {
            override fun onResult(result: String) {}
        }

        fun secondsForTry(executionTry: Int): Int = Math.pow(3.0, executionTry.toDouble()).toInt()
    }
}
