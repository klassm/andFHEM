package li.klass.fhem.connection.backend

import java.util.regex.Pattern

val explicitPortPattern: Pattern = Pattern.compile(":([\\d]+)")

fun extractPortFrom(url: String): Int? {

    val matcher = url.let { explicitPortPattern.matcher(it) }
    return when {
        matcher.find() -> {
            Integer.valueOf(matcher.group(1)!!)
        }
        url.startsWith("https://") -> 443
        else -> if (url.startsWith("http://")) 80 else 0
    }
}