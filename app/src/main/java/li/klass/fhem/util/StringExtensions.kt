package li.klass.fhem.util

import org.apache.commons.lang3.StringUtils

fun String?.trimToNull(): String? = this?.let { StringUtils.trimToNull(it) }