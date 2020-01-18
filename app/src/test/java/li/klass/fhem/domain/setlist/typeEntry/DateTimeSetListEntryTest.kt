package li.klass.fhem.domain.setlist.typeEntry

import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(DataProviderRunner::class)
class DateTimeSetListEntryTest {
    @Test
    @UseDataProvider("configProvider")
    fun should_parse(testCase: ConfigParseTestCase) {
        val result = DateTimeSetListEntry.parseConfig(testCase.parts)
        assertThat(result).isEqualTo(testCase.expected)
    }

    data class ConfigParseTestCase(val parts: List<String>, val expected: Config)

    companion object {
        private val defaultConfig = Config(
                timePicker = true,
                datePicker = true,
                format = "dd.MM.yyyy",
                step = 60
        )

        @DataProvider
        @JvmStatic
        fun configProvider(): List<ConfigParseTestCase> = listOf(
                ConfigParseTestCase(
                        parts = listOf(
                                "datetime",
                                "timepicker:false",
                                "format:d.m.Y"
                        ),
                        expected = defaultConfig.copy(
                                timePicker = false,
                                format = "dd.MM.yyyy"
                        )
                ),
                ConfigParseTestCase(
                        parts = listOf(
                                "datetime:false",
                                "timepicker:true",
                                "format:d.m.Y H.i"
                        ),
                        expected = defaultConfig.copy(
                                timePicker = true,
                                datePicker = true,
                                format = "dd.MM.yyyy HH.mm"
                        )
                ),
                ConfigParseTestCase(
                        parts = listOf(
                                "datetime"
                        ),
                        expected = defaultConfig
                ),
                ConfigParseTestCase(
                        parts = listOf(
                                "datetime", "step:30"
                        ),
                        expected = defaultConfig.copy(
                                step = 30
                        )
                ),
                ConfigParseTestCase(
                        parts = listOf(
                                "datetime", "step:bla"
                        ),
                        expected = defaultConfig.copy(
                                step = 60
                        )
                ),
                ConfigParseTestCase(
                        parts = listOf(
                                "datetime", "step:53"
                        ),
                        expected = defaultConfig
                )
        )
    }
}