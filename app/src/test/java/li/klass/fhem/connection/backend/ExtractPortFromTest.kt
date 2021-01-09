package li.klass.fhem.connection.backend

import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(DataProviderRunner::class)
class ExtractPortFromTest {
    @UseDataProvider("ports")
    @Test
    fun can_extract_the_port_of_a_url(test: TestCase) {
        assertThat(extractPortFrom(test.url)).`as`(test.url).isEqualTo(test.expectedPort)
    }

    companion object {
        @DataProvider
        @JvmStatic
        fun ports() = listOf(
                TestCase(
                        url = "http://192.168.0.200:8084/fhem",
                        expectedPort = 8084
                ),
                TestCase(
                        url = "http://192.168.0.200/fhem",
                        expectedPort = 80
                ),
                TestCase(
                        url = "https://192.168.0.200/fhem",
                        expectedPort = 443
                ),
                TestCase(
                        url = "https://www.test.bla.blub",
                        expectedPort = 443
                )
        )
    }

    data class TestCase(val url: String, val expectedPort: Int?)
}