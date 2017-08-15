package li.klass.fhem.util

import org.joda.time.DateTime
import javax.inject.Inject

class DateTimeProvider @Inject constructor() {
    fun now(): DateTime = DateTime.now()
}