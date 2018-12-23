package li.klass.fhem.util

import java.io.PrintWriter
import java.io.StringWriter

fun Exception.stackTraceAsString(): String {
    val writer = StringWriter()
    printStackTrace(PrintWriter(writer))
    return writer.toString()
}