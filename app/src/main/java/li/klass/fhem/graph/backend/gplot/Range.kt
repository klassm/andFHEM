package li.klass.fhem.graph.backend.gplot

import java.io.Serializable

data class Range(val lowerInclusive: Float? = null, val upperInclusive: Float? = null) : Serializable {
    companion object {
        fun atMost(valueInclusive: Float): Range =
                Range(lowerInclusive = null, upperInclusive = valueInclusive)

        fun atLeast(valueInclusive: Float): Range =
                Range(lowerInclusive = valueInclusive, upperInclusive = null)

        fun closed(lowerInclusive: Float, upperInclusive: Float): Range =
                Range(lowerInclusive = lowerInclusive, upperInclusive = upperInclusive)
    }
}