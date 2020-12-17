package li.klass.fhem.graph.backend;

import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.*
import org.junit.Test

class GraphDefinitionsForDeviceServiceTest {

    private fun getDeviceFor(property: String, value: String?) = XmlListDevice("SVG").apply {
        value?.let {
            attributes[property] = DeviceNode(DeviceNode.DeviceNodeType.ATTR, property, value, null as DateTime?)
        }
    }

    @Test
    fun fixedrangeFor() {
        val testdata = mapOf(
                "5hours" to Pair(Hours.hours(5), Hours.hours(0)),
                "day -2" to Pair(Days.days(1), Days.days(-2)),
                "week" to Pair(Weeks.weeks(1), Weeks.weeks(0)),
                "12months -10" to Pair(Months.months(12), Months.months(-120)),
                "2years -1" to Pair(Years.years(2), Years.years(-2)),
                "" to null,
                null to null
        )
        for ((input, output) in testdata) {
            assertThat(GraphDefinitionsForDeviceService.fixedrangeFor(getDeviceFor("fixedrange", input))).isEqualTo(output)
        }
    }

    @Test
    fun plotReplaceMapFor() {
        val testdata = mapOf(
                "" to emptyMap(),
                null to emptyMap(),
                "INTERVAL=month" to mapOf("INTERVAL" to "month"),
                """A="test 2 3" B={"to do" } C="x {y} z" """ to mapOf("A" to "test 2 3", "B" to """"to do" """, "C" to "x {y} z"),
                """"X=Y + 3"""" to mapOf("X" to "Y + 3"),
                "SOURCE=PowerDay UNIT=kWh" to mapOf("SOURCE" to "PowerDay", "UNIT" to "kWh")
        )
        for ((input, output) in testdata) {
            assertThat(GraphDefinitionsForDeviceService.plotReplaceMapFor(getDeviceFor("plotReplace", input))).isEqualTo(output)
        }
    }
}
