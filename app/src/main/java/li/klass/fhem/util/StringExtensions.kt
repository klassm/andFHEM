package li.klass.fhem.util

import android.text.Html
import org.apache.commons.lang3.StringUtils

fun String?.trimToNull(): String? = this?.let { StringUtils.trimToNull(it) }

fun String.toHtml() = if (contains("<")) Html.fromHtml(this) else this