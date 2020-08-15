package li.klass.fhem.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface Logging {
    val logger: Logger
        get() = logger(getClassForLogging(this::class.java))
}

fun <T : Any> getClassForLogging(javaClass: Class<T>): Class<*> = javaClass

inline fun <reified T : Logging> T.logger(classForLogging: Class<*>): Logger =
        LoggerFactory.getLogger(classForLogging)
