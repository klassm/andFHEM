package li.klass.fhem.domain.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DevStateIconsTest {
    @Test
    fun should_get_mapping_for_regex() {
        val result = DevStateIcons.parse("connected:10px-kreis-gelb .*disconnect:10px-kreis-rot .*done:10px-kreis-gruen ")

        assertThat(result.iconFor("bladisconnect")).isEqualTo("10px-kreis-rot")
        assertThat(result.iconFor("blaconnect")).isNull()
    }

    @Test
    fun should_get_mapping_for_non_regex() {
        val result = DevStateIcons.parse("connected:10px-kreis-gelb .*disconnect:10px-kreis-rot .*done:10px-kreis-gruen ")

        assertThat(result.iconFor("connected")).isEqualTo("10px-kreis-gelb")
    }

    @Test
    fun should_get_mapping_for_null_value() {
        val result = DevStateIcons.parse(null)

        assertThat(result.definitions).isEmpty()
    }
}