package li.klass.fhem.domain.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DevStateIconsTest {
    @Test
    fun should_get_mapping_for_regex() {
        val result = DevStateIcons.parse("connected:10px-kreis-gelb .*disconnect:10px-kreis-rot .*done:10px-kreis-gruen ")

        assertThat(result.iconFor("bladisconnect")).isEqualTo(DevStateIcons.DevStateIcon("10px-kreis-rot", false))
        assertThat(result.iconFor("blaconnect")).isNull()
    }

    @Test
    fun should_get_mapping_for_non_regex() {
        val result = DevStateIcons.parse("connected:10px-kreis-gelb .*disconnect:10px-kreis-rot .*done:10px-kreis-gruen ")

        assertThat(result.iconFor("connected")).isEqualTo(DevStateIcons.DevStateIcon("10px-kreis-gelb", false))
    }

    @Test
    fun should_get_mapping_for_noFhemwebLink() {
        val result = DevStateIcons.parse("connected:10px-kreis-gelb:noFhemwebLink")

        assertThat(result.iconFor("connected")).isEqualTo(DevStateIcons.DevStateIcon("10px-kreis-gelb", true))
    }

    @Test
    fun should_get_mapping_for_null_value() {
        val result = DevStateIcons.parse(null)

        assertThat(result.definitions).isEmpty()
    }

    @Test
    fun should_get_mapping_for_invalid_regex() {
        val result = DevStateIcons.parse("[:10px-kreis-gelb .*disconnect:10px-kreis-rot .*done:10px-kreis-gruen ")

        assertThat(result).isNotNull() // even if the first regexp is invalid!
        assertThat(result.iconFor("bladisconnect")).isEqualTo(DevStateIcons.DevStateIcon("10px-kreis-rot", false))
    }


    @Test
    fun should_find_out_whether_a_noFhemwebLinkRow_concerns_states() {
        val result = DevStateIcons.parse("on:green:noFhemwebLink o.*:red:noFhemwebLink unknown:yellow")

        assertThat(result.anyNoFhemwebLinkOf(listOf("on"))).isTrue()
        assertThat(result.anyNoFhemwebLinkOf(listOf("off"))).isTrue()
        assertThat(result.anyNoFhemwebLinkOf(listOf("on", "off"))).isTrue()
        assertThat(result.anyNoFhemwebLinkOf(listOf("on", "blub"))).isTrue()
        assertThat(result.anyNoFhemwebLinkOf(listOf("unknown", "blub"))).isFalse()
        assertThat(result.anyNoFhemwebLinkOf(listOf("unknown"))).isFalse()
    }
}