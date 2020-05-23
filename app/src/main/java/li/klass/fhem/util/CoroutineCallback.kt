package li.klass.fhem.util

import org.slf4j.LoggerFactory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface Callback<T> {
    fun onComplete(result: T)
    fun onException(e: Exception?)
}

suspend fun <T> awaitCallback(toExecute: (Callback<T>) -> Unit): T {
    return suspendCoroutine { cont ->
        var isResumed = false
        toExecute(object : Callback<T> {
            override fun onComplete(result: T) {
                if (!isResumed) {
                    isResumed = true
                    cont.resume(result)
                } else {
                    LoggerFactory.getLogger(Callback::class.java).error("Cannot resume callback more than once (onComplete)")
                }
            }

            override fun onException(e: Exception?) {
                if (!isResumed) {
                    isResumed = true
                    e?.let { cont.resumeWithException(it) }
                } else {
                    LoggerFactory.getLogger(Callback::class.java).error("Cannot resume callback more than once (onException)")
                }
            }
        })
    }
}