package li.klass.fhem.graph.backend.gplot

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class GPlotDefinitionTest {
    @Test
    fun can_serialize() {
        val definition = GPlotDefinitionTestdataBuilder.defaultGPlotDefinition()
        val byteArrayOutputStream = ByteArrayOutputStream()

        ObjectOutputStream(byteArrayOutputStream).use { it.writeObject(definition) }
        val array = byteArrayOutputStream.toByteArray()
        assertThat(array).isNotEmpty()

        val result = ObjectInputStream(ByteArrayInputStream(array)).use { it.readObject() }
        assertThat(result).isEqualTo(definition)
    }
}