package li.klass.fhem.graph.backend

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import li.klass.fhem.update.backend.command.execution.Command
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import li.klass.fhem.util.DateFormatUtil
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.Hours
import org.joda.time.Interval
import org.junit.Before
import org.junit.Test

class GraphIntervalProviderTest {
    @MockK
    private lateinit var commandExecutionService: CommandExecutionService

    @MockK
    private lateinit var context: Context

    private lateinit var graphIntervalProvider: GraphIntervalProvider

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { commandExecutionService.executeSync(Command("{{ TimeNow() }}")) } returns "2020-03-10 20:57:40"
        graphIntervalProvider = spyk(GraphIntervalProvider(commandExecutionService))
        every { graphIntervalProvider.getChartingDefaultTimespan(any()) } returns 24
    }

    @Test
    fun getIntervalFromStartEnd() {
        assertThat(graphIntervalProvider.getIntervalFor(
                DateFormatUtil.FHEM_DATE_FORMAT.parseDateTime("2020-03-09 10:58:41"),
                DateFormatUtil.FHEM_DATE_FORMAT.parseDateTime("2020-03-10 20:57:40"),
                null,
                context, null)
        ).isEqualTo(
                Interval(
                        DateFormatUtil.FHEM_DATE_FORMAT.parseDateTime("2020-03-09 10:58:41"),
                        DateFormatUtil.FHEM_DATE_FORMAT.parseDateTime("2020-03-10 20:57:40")
                )
        )
    }

    @Test
    fun getIntervalFromContext() {
        assertThat(graphIntervalProvider.getIntervalFor(null, null, null, context, null)).isEqualTo(
                Interval(DateFormatUtil.FHEM_DATE_FORMAT.parseDateTime("2020-03-09 20:57:40"),
                        DateFormatUtil.FHEM_DATE_FORMAT.parseDateTime("2020-03-10 20:57:40")))
    }

    @Test
    fun getIntervalFromFixedRange() {
        assertThat(graphIntervalProvider.getIntervalFor(null, null, Pair(Hours.hours(3), Hours.hours(-6)), context, null)).isEqualTo(
                Interval(DateFormatUtil.FHEM_DATE_FORMAT.parseDateTime("2020-03-10 11:57:40"),
                        DateFormatUtil.FHEM_DATE_FORMAT.parseDateTime("2020-03-10 14:57:40")))
    }
}