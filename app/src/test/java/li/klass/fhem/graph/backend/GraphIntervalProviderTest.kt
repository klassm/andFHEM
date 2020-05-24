package li.klass.fhem.graph.backend

import android.content.Context
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import li.klass.fhem.update.backend.command.execution.Command
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import li.klass.fhem.util.DateFormatUtil
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.Hours
import org.joda.time.Interval
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class GraphIntervalProviderTest {
    @Mock
    private lateinit var commandExecutionService: CommandExecutionService

    @Mock
    private lateinit var context: Context

    private lateinit var graphIntervalProvider: GraphIntervalProvider

    private fun <T> any(): T {
        Mockito.any<T>()
        return uninitialized()
    }

    private fun <T> uninitialized(): T = null as T

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        given(commandExecutionService.executeSync(Command("{{ TimeNow() }}"))).willReturn("2020-03-10 20:57:40")
        graphIntervalProvider = spy(GraphIntervalProvider(commandExecutionService))
        doReturn(24).whenever(graphIntervalProvider).getChartingDefaultTimespan(any())
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